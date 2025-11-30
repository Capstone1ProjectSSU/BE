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
    private Difficulty difficulty;
    private String instrument;
    private String key;

    public void validate() {
        if (title == null && artist == null && difficulty == null &&
                instrument == null && key == null) {
            throw new IllegalArgumentException("수정할 필드가 하나 이상 필요합니다.");
        }

        if (title != null && title.trim().isEmpty()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다.");
        }
        if (artist != null && artist.trim().isEmpty()) {
            throw new IllegalArgumentException("아티스트는 비어있을 수 없습니다.");
        }
    }
}