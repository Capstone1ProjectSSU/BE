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
@Table(name = "transcription_job")
public class TranscriptionJob extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "audio_id", nullable = false)
    private Long audioId;

    @Column(name = "sheet_id")
    private Long sheetId;

    @Column(name = "ai_job_id", length = 100)
    private String aiJobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "progress_stage", nullable = false, length = 20)
    private ProgressStage progressStage;

    @Column(name = "progress_percent")
    private Integer progressPercent;

    @Column(name = "instrument", length = 50)
    private String instrument;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", length = 20)
    @Builder.Default
    private JobType jobType = JobType.TRANSCRIPTION;

    private String errorMessage;
    private LocalDateTime queuedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;

    public void updateAiJobId(String aiJobId) { this.aiJobId = aiJobId; }
    public void updateStatus(ProgressStage status) { this.progressStage = status; /* 날짜 업데이트 로직 */ }
    public void updateProgressPercent(Integer p) { this.progressPercent = p; }
    public void updateSheetId(Long sheetId) { this.sheetId = sheetId; }
    public void updateErrorMessage(String msg) { this.errorMessage = msg; }

    public static TranscriptionJob create(Long userId, Long audioId, String instrument) {
        return TranscriptionJob.builder().userId(userId).audioId(audioId).instrument(instrument).jobType(JobType.TRANSCRIPTION).progressStage(ProgressStage.PENDING).queuedAt(LocalDateTime.now()).build();
    }
    public static TranscriptionJob createDifficultyJob(Long userId, Long audioId, Long sheetId, JobType jobType) {
        return TranscriptionJob.builder().userId(userId).audioId(audioId).sheetId(sheetId).instrument("guitar").jobType(jobType).progressStage(ProgressStage.PENDING).queuedAt(LocalDateTime.now()).build();
    }
}