package com.example.cap1.domain.sheet.domain;

import com.example.cap1.global.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "Sheet")
public class Sheet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userId", nullable = false)
    private Long userId;

    @Column(name = "audioId", nullable = false)
    private Long audioId;

    @Column(name = "title")
    private String title;

    @Column(name = "artist")
    private String artist;

    @Column(name = "instrument", length = 100)
    private String instrument;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty;

    @Column(name = "tuning", length = 50)
    private String tuning;

    @Column(name = "capo")
    private Integer capo;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "tempo")
    private Integer tempo;

    @Column(name = "`key`", length = 10)
    private String key;

    @Column(name = "sheetDataUrl", length = 500)
    private String sheetDataUrl;

    @Column(name = "thumbnailUrl", length = 500)
    private String thumbnailUrl;

    /**
     * 악보 정보를 수정합니다.
     */
    public void update(String title, String artist, String instrument,
                       Difficulty difficulty, String tuning, Integer capo,
                       Integer tempo, String key) {
        if (title != null) {
            this.title = title;
        }
        if (artist != null) {
            this.artist = artist;
        }
        if (instrument != null) {
            this.instrument = instrument;
        }
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        if (tuning != null) {
            this.tuning = tuning;
        }
        if (capo != null) {
            this.capo = capo;
        }
        if (tempo != null) {
            this.tempo = tempo;
        }
        if (key != null) {
            this.key = key;
        }
    }
}