package com.example.cap1.domain.transcription.domain;

import com.example.cap1.global.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "TranscriptionJob")
public class TranscriptionJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "audioId", nullable = false)
    private Long audioId;

    @Column(name = "sheetId")
    private Long sheetId;

    @Column(name = "aiJobId", length = 100)
    private String aiJobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "progressStage", nullable = false, length = 20)
    private ProgressStage progressStage;

    @Column(name = "progressPercent")
    private Integer progressPercent;

    @Column(name = "instrument", length = 50)
    private String instrument;

    @Column(name = "queuedAt")
    private LocalDateTime queuedAt;

    @Column(name = "startedAt")
    private LocalDateTime startedAt;

    @Column(name = "completedAt")
    private LocalDateTime completedAt;

    @Column(name = "failedAt")
    private LocalDateTime failedAt;

    @Column(name = "errorMessage", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * AI Job ID 업데이트
     */
    public void updateAiJobId(String aiJobId) {
        this.aiJobId = aiJobId;
    }

    /**
     * 전체 상태 업데이트
     */
    public void updateStatus(ProgressStage status) {
        this.progressStage = status;

        if (status == ProgressStage.PROCESSING && this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        } else if (status == ProgressStage.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        } else if (status == ProgressStage.FAILED) {
            this.failedAt = LocalDateTime.now();
        }
    }

    /**
     * 진행률 업데이트
     */
    public void updateProgressPercent(Integer progressPercent) {
        this.progressPercent = progressPercent;
    }

    /**
     * Sheet ID 업데이트
     */
    public void updateSheetId(Long sheetId) {
        this.sheetId = sheetId;
    }

    /**
     * 에러 메시지 업데이트
     */
    public void updateErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * TranscriptionJob 생성 팩토리 메서드
     */
    public static TranscriptionJob create(Long userId, Long audioId, String instrument) {
        return TranscriptionJob.builder()
                .userId(userId)
                .audioId(audioId)
                .instrument(instrument)
                .progressStage(ProgressStage.PENDING)
                .progressPercent(0)
                .queuedAt(LocalDateTime.now())
                .build();
    }

    @Column(name = "sheetDataUrl", length = 500)
    private String sheetDataUrl;

    public void setSheetDataUrl(String url) {
        this.sheetDataUrl = url;
    }

    public String getSheetDataUrl() {
        return this.sheetDataUrl;
    }
}