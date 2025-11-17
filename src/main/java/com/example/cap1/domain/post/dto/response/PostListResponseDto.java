package com.example.cap1.domain.post.dto.response;

import com.example.cap1.domain.sheet.domain.Difficulty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostListResponseDto {
    private Long postId;
    private String title;
    private String artist;
    private String instrument;
    private Difficulty difficulty;
    private Float rating;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
