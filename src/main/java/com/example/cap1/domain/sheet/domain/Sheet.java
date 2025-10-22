package com.example.cap1.domain.sheet.domain;
import com.example.cap1.global.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Sheet extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String artist;
    private String instrument;
    private Difficulty difficulty;
    private String tuning;
    private Long capo;
    private Long duration;
    private Long tempo;
    private String key;
    private String sheetDataUrl;
    private String thumbnailUrl;



}
