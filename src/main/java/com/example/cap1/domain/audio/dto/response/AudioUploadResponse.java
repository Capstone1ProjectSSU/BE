package com.example.cap1.domain.audio.dto.response;

import com.example.cap1.domain.audio.domain.Audio;
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
public class AudioUploadResponse {

    private String audioId;
    private Long fileSize;
    private String filePath;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime uploadedAt;

    public static AudioUploadResponse from(Audio audio) {
        return AudioUploadResponse.builder()
                .audioId(String.valueOf(audio.getId()))
                .fileSize(audio.getFileSize())
                .filePath(audio.getFilePath())
                .uploadedAt(audio.getUploadedAt())
                .build();
    }
}