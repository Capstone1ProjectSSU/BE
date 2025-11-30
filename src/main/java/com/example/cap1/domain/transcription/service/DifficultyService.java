package com.example.cap1.domain.transcription.service;

import com.example.cap1.domain.sheet.domain.Sheet;
import com.example.cap1.domain.sheet.repository.SheetRepository;
import com.example.cap1.domain.transcription.client.AiServerClient;
import com.example.cap1.domain.transcription.domain.JobType;
import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import com.example.cap1.domain.transcription.dto.ai.AiEnqueueResponse;
import com.example.cap1.domain.transcription.dto.response.TranscriptionStatusResponse;
import com.example.cap1.domain.transcription.repository.TranscriptionJobRepository;
import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DifficultyService {
    private final TranscriptionJobRepository jobRepository;
    private final SheetRepository sheetRepository;
    private final AiServerClient aiServerClient;

    public DifficultyResponse requestDifficultyChange(Long userId, Long sheetId, JobType jobType) {
        Sheet originSheet = sheetRepository.findById(sheetId).orElseThrow(() -> new GeneralException(Code.SHEET_NOT_FOUND));
        TranscriptionJob job = TranscriptionJob.createDifficultyJob(userId, originSheet.getAudioId(), sheetId, jobType);
        jobRepository.save(job);
        AiEnqueueResponse res = aiServerClient.enqueueDifficultyTask(originSheet.getSheetDataUrl(), jobType);
        job.updateAiJobId(res.getJobId());
        job.updateStatus(ProgressStage.PROCESSING);
        return DifficultyResponse.builder().jobId(String.valueOf(job.getId())).aiJobId(res.getJobId()).status("PROCESSING").build();
    }

    @Transactional(readOnly = true)
    public DifficultyResponse getJobStatus(Long userId, Long jobId) {
        TranscriptionJob job = jobRepository.findById(jobId).orElseThrow(() -> new GeneralException(Code.JOB_NOT_FOUND));
        String newSheetId = null;
        TranscriptionStatusResponse.AvailableResults availableResults = null;

        if (job.getProgressStage() == ProgressStage.COMPLETED) {
            if (job.getSheetId() != null) newSheetId = String.valueOf(job.getSheetId());
            availableResults = TranscriptionStatusResponse.AvailableResults.builder()
                    .chordProgression(TranscriptionStatusResponse.AvailableResults.ChordProgression.builder()
                            .jsonUrl("/api/transcription/download/" + job.getAiJobId() + "/chords/json")
                            .build())
                    .build();
        }
        return DifficultyResponse.builder().jobId(String.valueOf(job.getId())).aiJobId(job.getAiJobId()).status(job.getProgressStage().name()).newSheetId(newSheetId).availableResults(availableResults).build();
    }

    @Getter @Builder
    public static class DifficultyResponse {
        private String jobId;
        private String aiJobId;
        private String status;
        private String newSheetId;
        private TranscriptionStatusResponse.AvailableResults availableResults;
    }
}