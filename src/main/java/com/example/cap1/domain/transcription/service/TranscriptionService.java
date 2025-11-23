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
    private final AiServerClient aiServerClient;

    @Value("${file.transcription-dir:./uploads/transcription}")
    private String transcriptionDir;

    @Value("${ai.server.mock-mode:true}")
    private boolean mockMode;

    /**
     * 🆕 서버 시작 시 더미 파일 준비
     */
    @PostConstruct
    public void initDummyFiles() {
        if (!mockMode) {
            log.info("Mock 모드가 아니므로 더미 파일 초기화 생략");
            return;
        }

        try {
            // transcription 디렉토리 생성
            Path targetBasePath = Paths.get(transcriptionDir);
            Files.createDirectories(targetBasePath);

            log.info("✅ 더미 파일 디렉토리 준비 완료 - 경로: {}", targetBasePath);
            log.info("💡 악보 생성 요청 시 자동으로 해당 aiJobId 디렉토리에 복사됩니다.");

        } catch (IOException e) {
            log.warn("⚠️ 더미 파일 초기화 실패 (테스트 시에만 필요) - {}", e.getMessage());
        }
    }

    /**
     * 🆕 Mock 모드일 때 더미 파일 복사
     */
    private void copyDummyFilesForJob(String aiJobId) {
        if (!mockMode) {
            return;
        }

        try {
            ClassPathResource dummyResource = new ClassPathResource("dummy/transcription/mock-ai-job-default");
            Path sourcePath = dummyResource.getFile().toPath();
            Path targetPath = Paths.get(transcriptionDir).resolve(aiJobId);

            // 이미 존재하면 스킵
            if (Files.exists(targetPath)) {
                log.debug("더미 파일이 이미 존재함: {}", targetPath);
                return;
            }

            // 디렉토리 복사
            FileSystemUtils.copyRecursively(sourcePath, targetPath);
            log.info("✅ Mock 모드: 더미 파일 복사 완료 - {}", targetPath);

        } catch (IOException e) {
            log.warn("⚠️ 더미 파일 복사 실패 (테스트에는 영향 없음) - {}", e.getMessage());
        }
    }

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
            savedJob.updateProgressPercent(0);

            transcriptionJobRepository.save(savedJob);

            // 🆕 Mock 모드일 때 더미 파일 복사
            copyDummyFilesForJob(aiResponse.getJobId());

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
     * 🆕 악보 생성 상태 조회 (v2 - 단계별 정보 포함)
     */
    public TranscriptionStatusResponse getTranscriptionStatus(Long jobId, Long userId) {
        log.info("악보 생성 상태 조회 - jobId: {}, userId: {}", jobId, userId);

        // 1. Job 조회
        TranscriptionJob job = transcriptionJobRepository.findById(jobId)
                .orElseThrow(() -> new GeneralException(Code.JOB_NOT_FOUND));

        // 2. 권한 확인
        if (!job.getUserId().equals(userId)) {
            log.warn("작업 접근 권한 없음 - jobId: {}, requestUserId: {}, ownerUserId: {}",
                    jobId, userId, job.getUserId());
            throw new GeneralException(Code.JOB_FORBIDDEN);
        }

        // 🆕 3. currentStage 결정
        String currentStage = determineCurrentStage(job);

        // 🆕 4. availableResults 생성
        TranscriptionStatusResponse.AvailableResults availableResults =
                buildAvailableResults(job);

        log.info("작업 상태 조회 완료 - jobId: {}, status: {}, stage: {}, progress: {}%",
                jobId, job.getProgressStage(), currentStage, job.getProgressPercent());

        return TranscriptionStatusResponse.from(job, currentStage, availableResults);
    }

    /**
     * 🆕 현재 단계 결정
     */
    private String determineCurrentStage(TranscriptionJob job) {
        if (job.getProgressStage() == ProgressStage.COMPLETED) {
            return "completed";
        } else if (job.getProgressStage() == ProgressStage.FAILED) {
            Integer progress = job.getProgressPercent();
            if (progress == null || progress < 35) return "separating";
            if (progress < 65) return "transcribing";
            if (progress < 95) return "recognizing_chords";
            return "generating_sheet";
        } else if (job.getProgressStage() == ProgressStage.PROCESSING) {
            Integer progress = job.getProgressPercent();
            if (progress == null || progress < 35) return "separating";
            if (progress < 65) return "transcribing";
            if (progress < 95) return "recognizing_chords";
            return "generating_sheet";
        }
        return "pending";
    }

    /**
     * 🆕 다운로드 가능한 파일 정보 생성
     */
    private TranscriptionStatusResponse.AvailableResults buildAvailableResults(
            TranscriptionJob job) {

        if (job.getProgressStage() == ProgressStage.PENDING) {
            return null;
        }

        String aiJobId = job.getAiJobId();
        Integer progress = job.getProgressPercent();

        if (progress == null) {
            progress = 0;
        }

        TranscriptionStatusResponse.AvailableResults.AvailableResultsBuilder builder =
                TranscriptionStatusResponse.AvailableResults.builder();

        // Stage 1 완료: 음원 분리 (35% 이상)
        if (progress >= 35) {
            builder.separatedTracks(
                    TranscriptionStatusResponse.AvailableResults.SeparatedTracks.builder()
                            .guitarUrl("/api/transcription/download/" + aiJobId + "/separated/guitar")
                            .bassUrl("/api/transcription/download/" + aiJobId + "/separated/bass")
                            .vocalUrl("/api/transcription/download/" + aiJobId + "/separated/vocal")
                            .drumsUrl("/api/transcription/download/" + aiJobId + "/separated/drums")
                            .build()
            );
        }

        // Stage 2 완료: MIDI 변환 (65% 이상)
        if (progress >= 65) {
            builder.midiUrl("/api/transcription/download/" + aiJobId + "/midi");
        }

        // Stage 3 완료: 코드 인지 (95% 이상)
        if (progress >= 95) {
            builder.chordProgression(
                    TranscriptionStatusResponse.AvailableResults.ChordProgression.builder()
                            .jsonUrl("/api/transcription/download/" + aiJobId + "/chords/json")
                            .txtUrl("/api/transcription/download/" + aiJobId + "/chords/txt")
                            .build()
            );
        }

        return builder.build();
    }
}