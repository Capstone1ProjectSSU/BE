package com.example.cap1.domain.transcription.scheduler;

import com.example.cap1.domain.transcription.client.AiServerClient;
import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import com.example.cap1.domain.transcription.dto.ai.AiResultResponse;
import com.example.cap1.domain.transcription.dto.ai.AiStatusResponse;
import com.example.cap1.domain.transcription.repository.TranscriptionJobRepository;
import com.example.cap1.domain.transcription.service.TranscriptionService;
import com.example.cap1.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TranscriptionJob의 상태를 주기적으로 확인하고 자동으로 처리하는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TranscriptionPollingScheduler {

    private final TranscriptionJobRepository transcriptionJobRepository;
    private final AiServerClient aiServerClient;
    private final TranscriptionService transcriptionService;

    /**
     * 3초마다 PROCESSING 상태의 작업들을 확인하고 처리
     */
    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void pollAiServerStatus() {
        try {
            // 1. PROCESSING 상태의 모든 작업 조회
            List<TranscriptionJob> processingJobs = transcriptionJobRepository
                    .findByProgressStage(ProgressStage.PROCESSING);

            if (processingJobs.isEmpty()) {
                log.debug("처리 중인 작업 없음 - 스킵");
                return;
            }

            log.info("=== 폴링 스케줄러 실행 - 처리 중인 작업: {}개 ===", processingJobs.size());

            // 2. 각 작업의 상태 확인 및 업데이트
            for (TranscriptionJob job : processingJobs) {
                try {
                    processJob(job);
                } catch (Exception e) {
                    log.error("작업 처리 실패 - jobId: {}, aiJobId: {}",
                            job.getId(), job.getAiJobId(), e);

                    // 에러 발생 시 FAILED 상태로 변경
                    job.updateStatus(ProgressStage.FAILED);
                    job.updateErrorMessage("폴링 처리 중 오류 발생: " + e.getMessage());
                    transcriptionJobRepository.save(job);
                }
            }

        } catch (Exception e) {
            log.error("폴링 스케줄러 전체 실행 실패", e);
        }
    }

    /**
     * 개별 작업 처리
     */
    private void processJob(TranscriptionJob job) {
        String aiJobId = job.getAiJobId();

        log.info("작업 상태 확인 - jobId: {}, aiJobId: {}", job.getId(), aiJobId);

        // 1. AI 서버에서 상태 조회
        AiStatusResponse aiStatus = aiServerClient.getTaskStatus(aiJobId);

        // 2. progressPercent 업데이트
        Integer newProgress = aiStatus.getProgressPercent();
        if (newProgress != null && !newProgress.equals(job.getProgressPercent())) {
            job.updateProgressPercent(newProgress);
            log.info("진행률 업데이트 - jobId: {}, {}% → {}%",
                    job.getId(), job.getProgressPercent(), newProgress);
        }

        // 3. 상태별 처리
        String aiStatusStr = aiStatus.getStatus();

        switch (aiStatusStr) {
            case "completed":
                handleCompletedJob(job, aiJobId);
                break;

            case "failed":
                handleFailedJob(job, aiStatus);
                break;

            case "processing":
                log.debug("작업 처리 중 - jobId: {}, progress: {}%",
                        job.getId(), newProgress);
                break;

            case "queued":
                log.debug("작업 대기 중 - jobId: {}", job.getId());
                break;

            default:
                log.warn("알 수 없는 AI 서버 상태 - jobId: {}, status: {}",
                        job.getId(), aiStatusStr);
        }

        transcriptionJobRepository.save(job);
    }

    /**
     * 완료된 작업 처리
     */
    private void handleCompletedJob(TranscriptionJob job, String aiJobId) {
        log.info("✅ 작업 완료 감지 - jobId: {}, aiJobId: {}", job.getId(), aiJobId);

        try {
            // 1. 결과 조회
            AiResultResponse result = aiServerClient.getTaskResult(aiJobId);

            // 2. 파일 다운로드
            log.info("📥 결과 파일 다운로드 시작 - aiJobId: {}", aiJobId);
            aiServerClient.downloadAllFiles(aiJobId, result);
            log.info("✅ 결과 파일 다운로드 완료");

            // 3. Sheet 생성
            log.info("📄 Sheet 생성 시작 - jobId: {}", job.getId());
            transcriptionService.createSheetFromCompletedJob(job, result);
            log.info("✅ Sheet 생성 완료");

            // 4. Job 상태 COMPLETED로 변경
            job.updateStatus(ProgressStage.COMPLETED);
            job.updateProgressPercent(100);

            log.info("🎉 작업 완료 처리 완료 - jobId: {}, sheetId: {}",
                    job.getId(), job.getSheetId());

        } catch (GeneralException e) {
            log.error("작업 완료 처리 실패 - jobId: {}", job.getId(), e);
            job.updateStatus(ProgressStage.FAILED);
            job.updateErrorMessage("완료 처리 실패: " + e.getMessage());
        }
    }

    /**
     * 실패한 작업 처리
     */
    private void handleFailedJob(TranscriptionJob job, AiStatusResponse aiStatus) {
        log.warn("❌ AI 서버 작업 실패 감지 - jobId: {}, aiJobId: {}",
                job.getId(), job.getAiJobId());

        job.updateStatus(ProgressStage.FAILED);

        // 에러 메시지 설정
        if (aiStatus.getError() != null) {
            String errorMessage = String.format("AI 서버 에러 [%s]: %s",
                    aiStatus.getError().getCode(),
                    aiStatus.getError().getMessage());
            job.updateErrorMessage(errorMessage);
        } else {
            job.updateErrorMessage("AI 서버에서 작업 실패");
        }

        log.info("작업 FAILED 상태로 변경 - jobId: {}", job.getId());
    }
}