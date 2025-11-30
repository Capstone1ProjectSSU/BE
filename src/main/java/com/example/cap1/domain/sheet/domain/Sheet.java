package com.example.cap1.domain.sheet.domain;

import com.example.cap1.global.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "sheet")
public class Sheet extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "audio_id", nullable = false)
    private Long audioId;

    private String title;

    private String artist;

    private String instrument;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column(name = "`key`", length = 10)
    private String key;

    @Column(name = "sheet_data_url", length = 500)
    private String sheetDataUrl;

    public void update(String title, String artist, String instrument, Difficulty difficulty, String key) {
        if (title != null) this.title = title;
        if (artist != null) this.artist = artist;
        if (difficulty != null) this.difficulty = difficulty;
        if (instrument != null) this.instrument = instrument;
        if (key != null) this.key = key;
    }
}