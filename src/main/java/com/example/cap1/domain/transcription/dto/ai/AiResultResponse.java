package com.example.cap1.domain.transcription.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버의 /result/{jobId} 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AiResultResponse {
    private String jobId;
    private Outputs outputs;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Outputs {
        // 음원 분리 결과
        @JsonProperty("separated_tracks")
        private SeparatedTracks separatedTracks;

        // MIDI 변환 결과
        @JsonProperty("transcription_url")
        private String transcriptionUrl;  // .mid 파일 URL

        // 코드 진행 결과
        @JsonProperty("chord_progression")
        private ChordProgression chordProgression;

        // 메타데이터
        private Metadata metadata;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeparatedTracks {
        @JsonProperty("guitar_track_url")
        private String guitarTrackUrl;

        @JsonProperty("bass_track_url")
        private String bassTrackUrl;

        @JsonProperty("vocal_track_url")
        private String vocalTrackUrl;

        @JsonProperty("drums_track_url")
        private String drumsTrackUrl;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChordProgression {
        @JsonProperty("json_url")
        private String jsonUrl;  // LLM-Chart JSON 파일 URL

        @JsonProperty("txt_url")
        private String txtUrl;   // 텍스트 파일 URL
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private Integer tempo;
        private String key;
        private Long duration;  // seconds

        @JsonProperty("time_signature")
        private String timeSignature;  // "4/4"
    }
}