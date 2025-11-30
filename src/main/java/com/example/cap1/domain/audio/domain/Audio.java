package com.example.cap1.domain.audio.domain;

import com.example.cap1.global.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Audio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String artist;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public static Audio create(Long userId, String title, String artist,
                               String filePath, Long fileSize) {
        return Audio.builder()
                .userId(userId)
                .title(title)
                .artist(artist)
                .filePath(filePath)
                .fileSize(fileSize)
                .uploadedAt(LocalDateTime.now())
                .build();
    }
}