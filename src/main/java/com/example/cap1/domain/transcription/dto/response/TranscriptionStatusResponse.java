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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TranscriptionStatusResponse {

    private String jobId;
    private String aiJobId;
    private ProgressStage status;
    private Integer progressPercent;
    private String instrument;

    // 🆕 v2 추가 필드
    private String currentStage;
    private AvailableResults availableResults;

    // 성공 시
    private String musicId;

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

    /**
     * 🆕 다운로드 가능한 중간 산출물 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AvailableResults {
        private SeparatedTracks separatedTracks;
        private String midiUrl;
        private ChordProgression chordProgression;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class SeparatedTracks {
            private String guitarUrl;
            private String bassUrl;
            private String vocalUrl;
            private String drumsUrl;
        }

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class ChordProgression {
            private String jsonUrl;
            private String txtUrl;
        }
    }

    /**
     * 기존 호환성 유지용 팩토리 메서드
     */
    public static TranscriptionStatusResponse from(TranscriptionJob job) {
        return from(job, null, null);
    }

    /**
     * 🆕 v2 팩토리 메서드 (currentStage, availableResults 포함)
     */
    public static TranscriptionStatusResponse from(
            TranscriptionJob job,
            String currentStage,
            AvailableResults availableResults) {

        return TranscriptionStatusResponse.builder()
                .jobId(String.valueOf(job.getId()))
                .aiJobId(job.getAiJobId())
                .status(job.getProgressStage())
                .progressPercent(job.getProgressPercent())
                .instrument(job.getInstrument())
                .currentStage(currentStage)
                .availableResults(availableResults)
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