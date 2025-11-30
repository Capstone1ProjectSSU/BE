package com.example.cap1.domain.transcription.dto.response;

import com.example.cap1.domain.transcription.domain.ProgressStage;
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
    private String jobType; // TRANSCRIPTION, EASIER, HARDER

    // Flat Structure for AI Artifacts
    private String transcriptionUrl;    // MIDI
    private String separatedAudioUrl;   // WAV/OPUS
    private String chordProgressionUrl; // JSON
    private String format;

    private String musicId; // 결과로 생성된 Sheet ID
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
}