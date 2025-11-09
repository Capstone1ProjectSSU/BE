package com.example.cap1.domain.transcription.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TranscriptionRequest {

    private Long audioId;
    private String instrument;

    /**
     * 요청 데이터 검증
     */
    public void validate() {
        if (audioId == null) {
            throw new IllegalArgumentException("audioId는 필수입니다.");
        }

        if (instrument == null || instrument.trim().isEmpty()) {
            throw new IllegalArgumentException("instrument는 필수입니다.");
        }

        // 지원하는 악기 검증
        if (!isSupportedInstrument(instrument)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 악기입니다. 지원 악기: guitar, bass"
            );
        }
    }

    private boolean isSupportedInstrument(String instrument) {
        return "guitar".equalsIgnoreCase(instrument)
                || "bass".equalsIgnoreCase(instrument);
    }
}