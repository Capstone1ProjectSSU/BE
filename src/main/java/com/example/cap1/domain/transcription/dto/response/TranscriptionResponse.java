package com.example.cap1.domain.transcription.dto.response;

import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResponse {

    private String jobId;
    private String aiJobId;
    private ProgressStage status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime queuedAt;

    // 프론트 Preview용 가짜 URL (mock)
    private String sheetDataUrl;

    public static TranscriptionResponse from(TranscriptionJob job) {
        return TranscriptionResponse.builder()
                .jobId(String.valueOf(job.getId()))
                .aiJobId(job.getAiJobId())
                .status(job.getProgressStage())
                .queuedAt(job.getQueuedAt())
                // 실제 파일 생성 없이, 규칙 기반 mock URL
                .sheetDataUrl("/files/sheets/music-" + job.getId() + ".json")
                .build();
    }
}