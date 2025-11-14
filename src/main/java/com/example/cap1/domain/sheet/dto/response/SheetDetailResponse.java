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

    private String musicId;
    private String title;
    private String artist;
    private String instrument;
    private Difficulty difficulty;
    private String tuning;
    private Integer capo;
    private Long duration;
    private Integer tempo;
    private String songKey;
    private String audioUrl;
    private String sheetDataUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    public static SheetDetailResponse of(Sheet sheet, Audio audio) {
        return SheetDetailResponse.builder()
                .musicId(String.valueOf(sheet.getId()))
                .title(sheet.getTitle())
                .artist(sheet.getArtist())
                .instrument(sheet.getInstrument())
                .difficulty(sheet.getDifficulty())
                .tuning(sheet.getTuning())
                .capo(sheet.getCapo())
                .duration(sheet.getDuration())
                .tempo(sheet.getTempo())
                .songKey(sheet.getSongKey())
                .audioUrl(audio.getFilePath())
                .sheetDataUrl(sheet.getSheetDataUrl())
                .createdAt(sheet.getCreatedAt())
                .updatedAt(sheet.getUpdatedAt())
                .build();
    }
}