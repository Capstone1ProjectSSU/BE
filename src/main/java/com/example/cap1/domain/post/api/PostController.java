package com.example.cap1.domain.post.api;

import com.example.cap1.domain.post.dto.response.PostShareResponseDto;
import com.example.cap1.domain.post.service.PostService;
import com.example.cap1.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    @PostMapping("/{postId}")
    public ResponseDto<PostShareResponseDto> postShare(@PathVariable Long postId) {
        PostShareResponseDto response = postService.updatePostShare(postId);
        return ResponseDto.of(response);
    }
}
