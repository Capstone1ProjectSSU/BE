package com.example.cap1.domain.transcription.dto.response;

import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // null 필드는 응답에서 제외
public class TranscriptionStatusResponse {

    private String jobId;
    private String aiJobId;
    private ProgressStage status;
    private Integer progressPercent;
    private String instrument;

    // 성공 시
    private String musicId;  // sheetId

    // 실패 시
    private String errorMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime queuedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime startedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime completedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime failedAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    public static TranscriptionStatusResponse from(TranscriptionJob job) {
        return TranscriptionStatusResponse.builder()
                .jobId(String.valueOf(job.getId()))
                .aiJobId(job.getAiJobId())
                .status(job.getProgressStage())
                .progressPercent(job.getProgressPercent())
                .instrument(job.getInstrument())
                .musicId(job.getSheetId() != null ? String.valueOf(job.getSheetId()) : null)
                .errorMessage(job.getErrorMessage())
                .queuedAt(job.getQueuedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .failedAt(job.getFailedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}