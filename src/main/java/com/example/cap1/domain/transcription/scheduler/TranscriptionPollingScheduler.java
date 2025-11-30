package com.example.cap1.domain.transcription.scheduler;

import com.example.cap1.domain.transcription.client.AiServerClient;
import com.example.cap1.domain.transcription.domain.JobType;
import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import com.example.cap1.domain.transcription.dto.ai.AiResultResponse;
import com.example.cap1.domain.transcription.dto.ai.AiStatusResponse;
import com.example.cap1.domain.transcription.repository.TranscriptionJobRepository;
import com.example.cap1.domain.transcription.service.DifficultyService;
import com.example.cap1.domain.transcription.service.TranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TranscriptionPollingScheduler {

    private final TranscriptionJobRepository jobRepository;
    private final AiServerClient aiServerClient;
    private final TranscriptionService transcriptionService;
    private final DifficultyService difficultyService;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void pollAiServerStatus() {
        List<TranscriptionJob> processingJobs = jobRepository.findByProgressStage(ProgressStage.PROCESSING);
        if (processingJobs.isEmpty()) return;

        for (TranscriptionJob job : processingJobs) {
            try {
                processJob(job);
            } catch (Exception e) {
                log.error("Job Processing Error [JobId: {}]: {}", job.getId(), e.getMessage());
                job.updateStatus(ProgressStage.FAILED);
                job.updateErrorMessage("Scheduler Error: " + e.getMessage());
                jobRepository.save(job);
            }
        }
    }

    private void processJob(TranscriptionJob job) {
        AiStatusResponse aiStatus = aiServerClient.getTaskStatus(job.getAiJobId(), job.getJobType());

        if (aiStatus.getProgressPercent() != null) {
            job.updateProgressPercent(aiStatus.getProgressPercent());
        }

        String statusStr = aiStatus.getStatus();
        if ("completed".equalsIgnoreCase(statusStr)) {
            handleCompletedJob(job);
        } else if ("failed".equalsIgnoreCase(statusStr)) {
            job.updateStatus(ProgressStage.FAILED);
            String errorMsg = (aiStatus.getError() != null) ? aiStatus.getError().getMessage() : "AI Server reported FAILED status.";
            job.updateErrorMessage(errorMsg);
            jobRepository.save(job);
        } else {
            jobRepository.save(job); // 진행률 업데이트 저장
        }
    }

    private void handleCompletedJob(TranscriptionJob job) {
        log.info("Job Completed [JobId: {}]. Processing result...", job.getId());

        AiResultResponse result = aiServerClient.getTaskResult(job.getAiJobId(), job.getJobType());

        // 작업 타입에 따라 적절한 서비스로 위임 (Delegation)
        if (job.getJobType() == JobType.TRANSCRIPTION) {
            aiServerClient.downloadAllFiles(job.getAiJobId(), result);
            transcriptionService.completeTranscriptionJob(job, result);
        } else {
            // EASIER, HARDER
            aiServerClient.downloadChordOnly(job.getAiJobId(), result, job.getJobType());
            difficultyService.completeDifficultyJob(job, result);
        }

        job.updateStatus(ProgressStage.COMPLETED);
        job.updateProgressPercent(100);
        log.info("Job Result Processed & Saved [JobId: {}]", job.getId());
    }
}