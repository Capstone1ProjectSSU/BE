package com.example.cap1.domain.transcription.service;

import com.example.cap1.domain.audio.domain.Audio;
import com.example.cap1.domain.audio.repository.AudioRepository;
import com.example.cap1.domain.post.converter.PostConverter;
import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.post.repository.PostRepository;
import com.example.cap1.domain.sheet.domain.Difficulty;
import com.example.cap1.domain.sheet.domain.Sheet;
import com.example.cap1.domain.sheet.repository.SheetRepository;
import com.example.cap1.domain.transcription.client.AiServerClient;
import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import com.example.cap1.domain.transcription.dto.ai.AiEnqueueResponse;
import com.example.cap1.domain.transcription.dto.ai.AiResultResponse;
import com.example.cap1.domain.transcription.dto.request.TranscriptionRequest;
import com.example.cap1.domain.transcription.dto.response.TranscriptionResponse;
import com.example.cap1.domain.transcription.dto.response.TranscriptionStatusResponse;
import com.example.cap1.domain.transcription.repository.TranscriptionJobRepository;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.domain.user.repository.UserRepository;
import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TranscriptionService {

    private final TranscriptionJobRepository transcriptionJobRepository;
    private final AudioRepository audioRepository;
    private final SheetRepository sheetRepository;
    private final AiServerClient aiServerClient;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Value("${file.transcription-dir:./uploads/transcription}")
    private String transcriptionDir;

    @Value("${ai.server.mock-mode:true}")
    private boolean mockMode;

    @PostConstruct
    public void initDummyFiles() {
        if (!mockMode) return;
        try {
            Files.createDirectories(Paths.get(transcriptionDir));
        } catch (IOException e) {
            log.warn("초기화 실패: {}", e.getMessage());
        }
    }

    private void copyDummyFilesForJob(String aiJobId) {
        if (!mockMode) return;
        try {
            // 더미 파일 복사 로직 (필요 시 구현)
        } catch (Exception e) {
            log.warn("더미 파일 복사 실패: {}", e.getMessage());
        }
    }

    @Transactional
    public TranscriptionResponse requestTranscription(Long userId, TranscriptionRequest request) {
        request.validate();

        Audio audio = audioRepository.findById(request.getAudioId())
                .orElseThrow(() -> new GeneralException(Code.AUDIO_NOT_FOUND));

        if (!audio.getUserId().equals(userId)) {
            throw new GeneralException(Code.AUDIO_FORBIDDEN);
        }

        boolean isProcessing = transcriptionJobRepository.existsByAudioIdAndProgressStageIn(
                audio.getId(),
                List.of(ProgressStage.PENDING, ProgressStage.PROCESSING)
        );

        if (isProcessing) {
            throw new GeneralException(Code.JOB_ALREADY_PROCESSING);
        }

        TranscriptionJob job = TranscriptionJob.create(userId, audio.getId(), request.getInstrument());
        TranscriptionJob savedJob = transcriptionJobRepository.save(job);

        try {
            AiEnqueueResponse aiResponse = aiServerClient.enqueueE2ETask(audio.getFilePath(), request.getInstrument());
            savedJob.updateAiJobId(aiResponse.getJobId());
            savedJob.updateStatus(ProgressStage.PROCESSING);
            savedJob.updateProgressPercent(0);
            transcriptionJobRepository.save(savedJob);

            copyDummyFilesForJob(aiResponse.getJobId());

            return TranscriptionResponse.from(savedJob);
        } catch (GeneralException e) {
            savedJob.updateStatus(ProgressStage.FAILED);
            savedJob.updateErrorMessage(e.getMessage());
            transcriptionJobRepository.save(savedJob);
            throw e;
        }
    }

    public TranscriptionStatusResponse getTranscriptionStatus(Long jobId, Long userId) {
        TranscriptionJob job = transcriptionJobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(Code.JOB_NOT_FOUND));

        if (!job.getUserId().equals(userId)) {
            throw new GeneralException(Code.JOB_FORBIDDEN);
        }

        String currentStage = determineCurrentStage(job);
        TranscriptionStatusResponse.AvailableResults availableResults = buildAvailableResults(job);

        return TranscriptionStatusResponse.from(job, currentStage, availableResults);
    }

    private String determineCurrentStage(TranscriptionJob job) {
        if (job.getProgressStage() == ProgressStage.COMPLETED) return "completed";
        return "processing"; // 단순화
    }

    private TranscriptionStatusResponse.AvailableResults buildAvailableResults(TranscriptionJob job) {
        if (job.getProgressStage() != ProgressStage.COMPLETED) return null;

        String aiJobId = job.getAiJobId();
        return TranscriptionStatusResponse.AvailableResults.builder()
                .separatedTracks(TranscriptionStatusResponse.AvailableResults.SeparatedTracks.builder()
                        .guitarUrl("/api/transcription/download/" + aiJobId + "/separated/guitar")
                        .bassUrl("/api/transcription/download/" + aiJobId + "/separated/bass")
                        .vocalUrl("/api/transcription/download/" + aiJobId + "/separated/vocal")
                        .drumsUrl("/api/transcription/download/" + aiJobId + "/separated/drums")
                        .build())
                .midiUrl("/api/transcription/download/" + aiJobId + "/midi")
                .chordProgression(TranscriptionStatusResponse.AvailableResults.ChordProgression.builder()
                        .jsonUrl("/api/transcription/download/" + aiJobId + "/chords/json")
                        .txtUrl("/api/transcription/download/" + aiJobId + "/chords/txt")
                        .build())
                .build();
    }

    @Transactional
    public void createSheetFromCompletedJob(TranscriptionJob job, AiResultResponse result) {
        Audio audio = audioRepository.findById(job.getAudioId())
                .orElseThrow(() -> new GeneralException(Code.AUDIO_NOT_FOUND));

        // 🆕 수정: Metadata 제거, Key 정보는 UnifiedProgression에서 가져오거나 없으면 null
        String key = null;
        if (result.getUnifiedProgression() != null) {
            key = result.getUnifiedProgression().getKey();
        }

        // 🆕 수정: tuning, capo, duration, tempo 필드 제거됨
        Sheet sheet = Sheet.builder()
                .userId(job.getUserId())
                .audioId(audio.getId())
                .title(audio.getTitle())
                .artist(audio.getArtist())
                .instrument(job.getInstrument())
                .difficulty(Difficulty.NORMAL)
                .key(key)
                // sheetDataUrl 설정 (다운로드 API 경로)
                .sheetDataUrl("/api/transcription/download/" + job.getAiJobId() + "/chords/json")
                .build();

        Sheet savedSheet = sheetRepository.save(sheet);

        User user = userRepository.findById(savedSheet.getUserId())
                .orElseThrow(() -> new GeneralException(Code.USER_NOT_FOUND));
        Post post = PostConverter.toPost(savedSheet, user);
        postRepository.save(post);

        job.updateSheetId(savedSheet.getId());
    }
}