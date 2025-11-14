package com.example.cap1.domain.transcription.service;

import com.example.cap1.domain.audio.domain.Audio;
import com.example.cap1.domain.audio.repository.AudioRepository;
import com.example.cap1.domain.transcription.client.AiServerClient;
import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import com.example.cap1.domain.transcription.dto.ai.AiEnqueueResponse;
import com.example.cap1.domain.transcription.dto.request.TranscriptionRequest;
import com.example.cap1.domain.transcription.dto.response.TranscriptionResponse;
import com.example.cap1.domain.transcription.dto.response.TranscriptionStatusResponse;
import com.example.cap1.domain.transcription.repository.TranscriptionJobRepository;
import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TranscriptionService {

    private final TranscriptionJobRepository transcriptionJobRepository;
    private final AudioRepository audioRepository;
    private final AiServerClient aiServerClient;

    /**
     * 악보 생성 요청 (E2E 파이프라인 시작)
     */
    @Transactional
    public TranscriptionResponse requestTranscription(
            Long userId,
            TranscriptionRequest request) {

        log.info("악보 생성 요청 시작 - userId: {}, audioId: {}, instrument: {}",
                userId, request.getAudioId(), request.getInstrument());

        // 1. 요청 데이터 검증
        request.validate();

        // 2. Audio 조회 및 권한 확인
        Audio audio = audioRepository.findById(request.getAudioId())
                .orElseThrow(() -> new GeneralException(Code.AUDIO_NOT_FOUND));

        if (!audio.getUserId().equals(userId)) {
            log.warn("음원 접근 권한 없음 - audioId: {}, requestUserId: {}, ownerUserId: {}",
                    request.getAudioId(), userId, audio.getUserId());
            throw new GeneralException(Code.AUDIO_FORBIDDEN);
        }

        // 3. 이미 처리 중인 작업이 있는지 확인
        boolean isProcessing = transcriptionJobRepository.existsByAudioIdAndProgressStageIn(
                audio.getId(),
                List.of(ProgressStage.PENDING, ProgressStage.PROCESSING)
        );

        if (isProcessing) {
            log.warn("이미 처리 중인 작업 존재 - audioId: {}", audio.getId());
            throw new GeneralException(Code.JOB_ALREADY_PROCESSING);
        }

        // 4. TranscriptionJob 생성
        TranscriptionJob job = TranscriptionJob.create(
                userId,
                audio.getId(),
                request.getInstrument()
        );

        TranscriptionJob savedJob = transcriptionJobRepository.save(job);

        log.info("TranscriptionJob 생성 완료 - jobId: {}", savedJob.getId());

        // 5. AI 서버에 E2E Task 등록
        try {
            AiEnqueueResponse aiResponse = aiServerClient.enqueueE2ETask(
                    audio.getFilePath(),
                    request.getInstrument()
            );

            // 6. AI Job ID 저장 및 상태 변경
            savedJob.updateAiJobId(aiResponse.getJobId());
            savedJob.updateStatus(ProgressStage.PROCESSING);

            transcriptionJobRepository.save(savedJob);

            log.info("AI 서버 E2E Task 등록 완료 - jobId: {}, aiJobId: {}",
                    savedJob.getId(), aiResponse.getJobId());

            return TranscriptionResponse.from(savedJob);

        } catch (GeneralException e) {
            // AI 서버 통신 실패 시 Job을 FAILED 상태로 변경
            savedJob.updateStatus(ProgressStage.FAILED);
            savedJob.updateErrorMessage(e.getMessage());
            transcriptionJobRepository.save(savedJob);

            log.error("AI 서버 E2E Task 등록 실패 - jobId: {}", savedJob.getId(), e);

            throw e;
        }
    }

    /**
     * Frontend Polling API
     * GET /api/transcription/request/{jobId}
     * */
    @Transactional
public TranscriptionResponse mockUpdateStatus(String jobId) {

    TranscriptionJob job = transcriptionJobRepository.findById(Long.parseLong(jobId))
            .orElseThrow(() -> new GeneralException(Code.JOB_NOT_FOUND));

    ProgressStage stage = job.getProgressStage();

    if (stage == ProgressStage.PENDING) {
        job.updateStatus(ProgressStage.PROCESSING);
    } 
    else if (stage == ProgressStage.PROCESSING) {
        job.updateStatus(ProgressStage.COMPLETED);

        generateMockSheetJson(job);
    }

    transcriptionJobRepository.save(job);

    return TranscriptionResponse.from(job);
}

private void generateMockSheetJson(TranscriptionJob job) {

    try {
        // 실제 저장 경로
        String baseDir = "/data/files/sheets/";
        File folder = new File(baseDir);
        if (!folder.exists()) folder.mkdirs();

        // 파일명 결정
        String filePath = baseDir + "music-" + job.getId() + ".json";

        // 프론트가 접근할 URL
        String publicUrl = "/data/files/music-" + job.getId() + ".json";

        // JSON 콘텐츠 생성
        String content = """
                {
                  "id": %d,
                  "title": "%s",
                  "artist": "%s",
                  "instrument": "%s",
                  "difficulty": "NORMAL",
                  "tempo": 120,
                  "capo": 0,
                  "createdAt": "%s",
                  "notes": [
                    { "string": 1, "fret": 3, "beat": 1.0 }
                  ]
                }
                """.formatted(
                job.getId(),
                "Preview Song",
                "Preview Artist",
                job.getInstrument(),
                LocalDateTime.now()
        );

        // 파일 저장
        Files.writeString(Path.of(filePath), content, StandardCharsets.UTF_8);

        // Job 에 URL 저장
        job.setSheetDataUrl(publicUrl);

    } catch (Exception e) {
        log.error("Mock sheet JSON 생성 실패", e);
        job.updateStatus(ProgressStage.FAILED);
    }
}

}