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
public class UpdateCommentResponseDto {

    private Long commentId;
    private String content;
    private Float rating;
    private LocalDateTime updatedAt;
}
