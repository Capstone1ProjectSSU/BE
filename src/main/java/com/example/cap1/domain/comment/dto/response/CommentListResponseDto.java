package com.example.cap1.domain.comment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentListResponseDto {
    private Long commentId;
    private String commentName;
    private String contents;
    private Float rating;
    LocalDateTime createdAt;
}
