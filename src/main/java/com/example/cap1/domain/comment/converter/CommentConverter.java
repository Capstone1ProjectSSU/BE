package com.example.cap1.domain.comment.converter;

import com.example.cap1.domain.comment.domain.Comment;
import com.example.cap1.domain.comment.dto.request.CreateCommentRequestDto;
import com.example.cap1.domain.comment.dto.response.CommentListResponseDto;
import com.example.cap1.domain.comment.dto.response.CreateCommentResponseDto;
import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentConverter {
    public static Comment toComment(User user, Post post, CreateCommentRequestDto requestDto) {
        return Comment.builder()
                .user(user)
                .post(post)
                .contents(requestDto.getContent())
                .rating(requestDto.getRating())
                .build();
    }

    public static CreateCommentResponseDto toCreateCommentResponseDto(Comment comment) {
        return CreateCommentResponseDto.builder()
                .commentId(comment.getId())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public static CommentListResponseDto toCommentListResponseDto(Comment c) {
        return CommentListResponseDto.builder()
                .commentId(c.getId())
                .contents(c.getContents())
                .rating(c.getRating())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
