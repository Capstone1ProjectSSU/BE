package com.example.cap1.domain.transcription.dto.ai;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiResultResponse {

    @JsonAlias({"jobId", "job_id"})
    private String jobId;

    // --- E2E Fields ---
    @JsonAlias({"transcriptionUrl", "transcription_url"})
    private String transcriptionUrl;

    @JsonAlias({"separatedAudioUrl", "separated_audio_url", "separated_tracks"})
    private JsonNode separatedAudioUrl;

    @JsonAlias({"chordProgressionUrl", "chord_progression_url"})
    private String chordProgressionUrl;

    @JsonAlias({"format"})
    private String format;

    // --- Difficulty Fields ---
    @JsonAlias({"easierChordProgressionUrl", "easier_chord_progression_url"})
    private String easierChordProgressionUrl;

    @JsonAlias({"complexifiedChordProgressionUrl", "complexified_chord_progression_url"})
    private String complexifiedChordProgressionUrl;

    @JsonAlias({"unifiedProgression", "unified_progression"})
    private UnifiedProgression unifiedProgression;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnifiedProgression {
        private String key;

        @JsonAlias({"timeSignature", "time_signature"})
        private String timeSignature;
    }
}