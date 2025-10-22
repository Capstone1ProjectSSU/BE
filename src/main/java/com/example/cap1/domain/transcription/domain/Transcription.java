package com.example.cap1.domain.transcription.domain;

import com.example.cap1.global.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Transcription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private ProgressStage progressStage;
    private LocalDateTime startAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private String errorMessage;
}
