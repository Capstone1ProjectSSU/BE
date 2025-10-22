package com.example.cap1.domain.transcription.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProgressStage {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
}
