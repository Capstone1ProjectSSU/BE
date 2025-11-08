package com.example.cap1.domain.sheet.dto.response;

import com.example.cap1.domain.sheet.domain.Difficulty;
import com.example.cap1.domain.sheet.domain.Sheet;
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
public class SheetSummaryDto {

    private String musicId;
    private String title;
    private String artist;
    private String instrument;
    private Difficulty difficulty;
    private String thumbnailUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    public static SheetSummaryDto from(Sheet sheet) {
        return SheetSummaryDto.builder()
                .musicId(String.valueOf(sheet.getId()))
                .title(sheet.getTitle())
                .artist(sheet.getArtist())
                .instrument(sheet.getInstrument())
                .difficulty(sheet.getDifficulty())
                .thumbnailUrl(sheet.getThumbnailUrl())
                .createdAt(sheet.getCreatedAt())
                .build();
    }
}