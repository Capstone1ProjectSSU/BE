package com.example.cap1.domain.transcription.scheduler;

import com.example.cap1.domain.sheet.domain.Difficulty;
import com.example.cap1.domain.sheet.domain.Sheet;
import com.example.cap1.domain.sheet.repository.SheetRepository;
import com.example.cap1.domain.transcription.client.AiServerClient;
import com.example.cap1.domain.transcription.domain.JobType;
import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import com.example.cap1.domain.transcription.dto.ai.AiResultResponse;
import com.example.cap1.domain.transcription.dto.ai.AiStatusResponse;
import com.example.cap1.domain.transcription.repository.TranscriptionJobRepository;
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
    private final SheetRepository sheetRepository;

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
        try {
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
                log.warn("Job {} failed by AI Server: {}", job.getId(), errorMsg);
            }
            jobRepository.save(job);

        } catch (Exception e) {
            throw new RuntimeException("Failed to poll status: " + e.getMessage(), e);
        }
    }

    private void handleCompletedJob(TranscriptionJob job) {
        log.info("Job Completed [JobId: {}]. Processing result...", job.getId());

        AiResultResponse result = aiServerClient.getTaskResult(job.getAiJobId(), job.getJobType());

        if (job.getJobType() == JobType.TRANSCRIPTION) {
            aiServerClient.downloadAllFiles(job.getAiJobId(), result);
            transcriptionService.createSheetFromCompletedJob(job, result);
        } else {
            aiServerClient.downloadChordOnly(job.getAiJobId(), result, job.getJobType());
            createDerivedSheet(job, result);
        }

        job.updateStatus(ProgressStage.COMPLETED);
        job.updateProgressPercent(100);
        log.info("Job Result Processed [JobId: {}]", job.getId());
    }

    private void createDerivedSheet(TranscriptionJob job, AiResultResponse result) {
        Long originSheetId = job.getSheetId();
        Sheet originSheet = sheetRepository.findById(originSheetId)
                .orElseThrow(() -> new RuntimeException("Original sheet not found: " + originSheetId));

        Difficulty newDifficulty = (job.getJobType() == JobType.EASIER) ? Difficulty.EASY : Difficulty.HARD;
        String titleSuffix = (job.getJobType() == JobType.EASIER) ? " (Easy)" : " (Hard)";

        String downloadPath = "/api/transcription/download/" + job.getAiJobId() + "/chords/json";

        Sheet newSheet = Sheet.builder()
                .userId(job.getUserId())
                .audioId(job.getAudioId())
                .title(originSheet.getTitle() + titleSuffix)
                .artist(originSheet.getArtist())
                .instrument(job.getInstrument())
                .difficulty(newDifficulty)
                .sheetDataUrl(downloadPath)
                .key(originSheet.getKey())
                .build();

        Sheet saved = sheetRepository.save(newSheet);
        job.updateSheetId(saved.getId());
    }
}