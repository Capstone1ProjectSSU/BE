package com.example.cap1.domain.transcription.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버의 /status/{jobId} 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiStatusResponse {
    private String jobId;
    private String status;  // "queued", "processing", "completed", "failed"
    private Integer progressPercent;

    // 🆕 v2 추가 필드
    private String currentStage;  // "separating", "transcribing", "recognizing_chords"
    private AvailableArtifacts availableArtifacts;

    private String queuedAt;
    private String startedAt;
    private String updatedAt;
    private String completedAt;
    private AiError error;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiError {
        private String code;
        private String message;
    }

    /**
     * 🆕 각 단계별로 가져올 수 있는 결과물
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailableArtifacts {
        private Boolean separatedTracksReady;
        private Boolean transcriptionReady;
        private Boolean chordProgressionReady;
    }
}