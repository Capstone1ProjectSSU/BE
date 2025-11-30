package com.example.cap1.domain.transcription.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiEnqueueResponse {
    private String jobId;
    private String status;
    private String queuedAt;
}