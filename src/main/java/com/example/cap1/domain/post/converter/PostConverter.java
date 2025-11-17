package com.example.cap1.domain.post.converter;

import com.example.cap1.domain.comment.dto.response.CommentListResponseDto;
import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.post.dto.response.PostListResponseDto;
import com.example.cap1.domain.post.dto.response.PostShareDetailResponseDto;
import com.example.cap1.domain.post.dto.response.PostShareResponseDto;

import java.util.List;

public class PostConverter {

    public static PostShareResponseDto toPostShareResponseDto(Post post) {
        return PostShareResponseDto.builder()
                .postId(post.getId())
                .share(post.getShare())
                .build();
    }

    public static PostShareDetailResponseDto toPostShareDetailResponseDto(Post post, List<CommentListResponseDto> commentList) {
        return PostShareDetailResponseDto.builder()
                .postId(post.getId())
                .postUserId(post.getUser().getId())
                .title(post.getSheet().getTitle())
                .artist(post.getSheet().getArtist())
                .instrument(post.getSheet().getInstrument())
                .difficulty(post.getSheet().getDifficulty())
                .rating(post.getRating())
                .sheetDataUrl(post.getSheet().getSheetDataUrl())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .commentList(commentList)
                .share(post.getShare())
                .build();
    }

    public static PostListResponseDto toPostListResponseDto(Post p) {
        return PostListResponseDto.builder()
                .postId(p.getId())
                .title(p.getSheet() != null ? p.getSheet().getTitle() : null)
                .artist(p.getSheet() != null ? p.getSheet().getArtist() : null)
                .instrument(p.getSheet() != null ? p.getSheet().getInstrument() : null)
                .difficulty(p.getSheet() != null ? p.getSheet().getDifficulty() : null)
                .rating(p.getRating())
                .commentCount(p.getCommentCount())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

}
