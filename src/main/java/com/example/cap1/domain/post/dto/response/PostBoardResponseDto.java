package com.example.cap1.domain.post.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostBoardResponseDto {
    private List<PostListResponseDto> board;

}
