package com.example.cap1.domain.post.dto.response;

import com.example.cap1.domain.comment.dto.response.CommentListResponseDto;
import com.example.cap1.domain.sheet.domain.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostShareDetailResponseDto {

    private Long postId;
    private Long postUserId;
    private String title;
    private String artist;
    private String instrument;
    private Difficulty difficulty;
    private Float rating;
    private String sheetDataUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    List<CommentListResponseDto> commentList;
    private Integer share;
    private boolean isMyPost;
}
