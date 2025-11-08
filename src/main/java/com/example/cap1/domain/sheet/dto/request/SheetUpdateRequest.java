package com.example.cap1.domain.sheet.dto.request;

import com.example.cap1.domain.sheet.domain.Difficulty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SheetUpdateRequest {

    private String title;
    private String artist;
    private String instrument;
    private Difficulty difficulty;
    private String tuning;
    private Integer capo;
    private Integer tempo;
    private String key;

    /**
     * 요청 데이터 유효성 검증
     */
    public void validate() {
        // 최소 하나의 필드는 수정되어야 함
        if (title == null && artist == null && instrument == null &&
                difficulty == null && tuning == null && capo == null &&
                tempo == null && key == null) {
            throw new IllegalArgumentException("수정할 필드가 하나 이상 필요합니다.");
        }

        // title 검증
        if (title != null && title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다.");
        }

        // artist 검증
        if (artist != null && artist.trim().isEmpty()) {
            throw new IllegalArgumentException("아티스트는 비어있을 수 없습니다.");
        }

        // capo 검증 (0-20 범위)
        if (capo != null && (capo < 0 || capo > 20)) {
            throw new IllegalArgumentException("카포는 0-20 사이의 값이어야 합니다.");
        }

        // tempo 검증 (20-300 범위)
        if (tempo != null && (tempo < 20 || tempo > 300)) {
            throw new IllegalArgumentException("템포는 20-300 사이의 값이어야 합니다.");
        }
    }
}