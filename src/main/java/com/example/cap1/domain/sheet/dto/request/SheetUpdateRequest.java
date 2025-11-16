package com.example.cap1.domain.sheet.dto.request;

import com.example.cap1.domain.sheet.domain.Difficulty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SheetUpdateRequest {

    // 현재 사용 필드
    private String title;
    private String artist;
    private Difficulty difficulty;

    // 추후 개발 예정 필드 (빈 값 허용)
    private String instrument;
    private String tuning;
    private Integer capo;
    private Integer tempo;
    private String key;

    /**
     * 요청 데이터 유효성 검증
     */
    public void validate() {
        // 최소 하나의 필드는 수정되어야 함
        if (title == null && artist == null && difficulty == null &&
                instrument == null && tuning == null && capo == null &&
                tempo == null && key == null) {
            throw new IllegalArgumentException("수정할 필드가 하나 이상 필요합니다.");
        }

        // title 검증 - null이 아니고 비어있지 않을 때만
        if (title != null && title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다.");
        }

        // artist 검증 - null이 아니고 비어있지 않을 때만
        if (artist != null && artist.trim().isEmpty()) {
            throw new IllegalArgumentException("아티스트는 비어있을 수 없습니다.");
        }

        // instrument, tuning, key, capo, tempo는 추후 개발 예정
        // 별도 검증 없음
    }
}