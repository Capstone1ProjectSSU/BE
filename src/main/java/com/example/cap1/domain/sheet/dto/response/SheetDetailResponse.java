package com.example.cap1.domain.sheet.dto.response;

import com.example.cap1.domain.audio.domain.Audio;
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
public class SheetDetailResponse {

    private String sheetId;
    private String title;
    private String artist;
    private String instrument;
    private Difficulty difficulty;
    private String key;
    private String audioUrl;
    private String sheetDataUrl;
    private Integer share;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    public static SheetDetailResponse of(Sheet sheet, Audio audio) {
        return SheetDetailResponse.builder()
                .sheetId(String.valueOf(sheet.getId()))
                .title(sheet.getTitle())
                .artist(sheet.getArtist())
                .instrument(sheet.getInstrument())
                .difficulty(sheet.getDifficulty())
                .key(sheet.getKey())
                .audioUrl(audio.getFilePath())
                .share(sheet.getShare())
                .sheetDataUrl(sheet.getSheetDataUrl())
                .createdAt(sheet.getCreatedAt())
                .updatedAt(sheet.getUpdatedAt())
                .build();
    }
}