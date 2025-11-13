package com.example.cap1.domain.post.converter;

import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.post.dto.response.PostShareResponseDto;

public class PostConverter {

    public static PostShareResponseDto toPostShareResponseDto(Post post) {
        return PostShareResponseDto.builder()
                .postId(post.getId())
                .share(post.getShare())
                .build();
    }
}
