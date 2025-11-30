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
import com.example.cap1.domain.transcription.domain.JobType;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
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

    @Value("${ai.server.mock-mode:false}")
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

    // --- E2E Enqueue Logic ---
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
            return TranscriptionResponse.from(transcriptionJobRepository.save(savedJob));
        } catch (GeneralException e) {
            savedJob.updateStatus(ProgressStage.FAILED);
            savedJob.updateErrorMessage(e.getMessage());
            transcriptionJobRepository.save(savedJob);
            throw e;
        }
    }

    // --- Unified Status Logic ---
    public TranscriptionStatusResponse getTranscriptionStatus(Long jobId, Long userId) {
        TranscriptionJob job = transcriptionJobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(Code.JOB_NOT_FOUND));

        if (!job.getUserId().equals(userId)) {
            throw new GeneralException(Code.JOB_FORBIDDEN);
        }

        String transcriptionUrl = null;
        String separatedAudioUrl = null;
        String chordProgressionUrl = null;
        String musicId = null;

        // 완료된 경우 URL 생성
        if (job.getProgressStage() == ProgressStage.COMPLETED) {
            String aiJobId = job.getAiJobId();

            // 공통: 코드 진행 URL
            chordProgressionUrl = "/api/transcription/download/" + aiJobId + "/chords/json";

            // E2E 작업일 경우에만 MIDI와 분리된 오디오 URL 제공
            if (job.getJobType() == JobType.TRANSCRIPTION) {
                String instrument = job.getInstrument() != null ? job.getInstrument() : "guitar";
                transcriptionUrl = "/api/transcription/download/" + aiJobId + "/midi";
                separatedAudioUrl = "/api/transcription/download/" + aiJobId + "/separated/" + instrument;
            }

            if (job.getSheetId() != null) {
                musicId = String.valueOf(job.getSheetId());
            }
        }

        return TranscriptionStatusResponse.builder()
                .jobId(String.valueOf(job.getId()))
                .aiJobId(job.getAiJobId())
                .status(job.getProgressStage())
                .progressPercent(job.getProgressPercent())
                .instrument(job.getInstrument())
                .jobType(job.getJobType().name())
                .musicId(musicId)
                // Flat Structure
                .transcriptionUrl(transcriptionUrl)
                .separatedAudioUrl(separatedAudioUrl)
                .chordProgressionUrl(chordProgressionUrl)
                .format("json")
                .errorMessage(job.getErrorMessage())
                .queuedAt(job.getQueuedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .failedAt(job.getFailedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    // --- Scheduler Delegation Method ---
    @Transactional
    public void completeTranscriptionJob(TranscriptionJob job, AiResultResponse result) {
        Audio audio = audioRepository.findById(job.getAudioId())
                .orElseThrow(() -> new GeneralException(Code.AUDIO_NOT_FOUND));

        String key = null;
        if (result.getUnifiedProgression() != null) {
            key = result.getUnifiedProgression().getKey();
        }

        Sheet sheet = Sheet.builder()
                .userId(job.getUserId())
                .audioId(audio.getId())
                .title(audio.getTitle())
                .artist(audio.getArtist())
                .instrument(job.getInstrument())
                .difficulty(Difficulty.NORMAL)
                .key(key)
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