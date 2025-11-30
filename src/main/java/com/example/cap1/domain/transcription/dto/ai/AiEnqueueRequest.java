package com.example.cap1.domain.transcription.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiEnqueueRequest {
    private String audioFileUrl;
    private String instrument;
}