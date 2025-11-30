package com.example.cap1.domain.post.api;

import com.example.cap1.domain.post.dto.response.PostBoardResponseDto;
import com.example.cap1.domain.post.dto.response.PostShareDetailResponseDto;
import com.example.cap1.domain.post.dto.response.PostShareResponseDto;
import com.example.cap1.domain.post.service.PostService;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    @PostMapping("/{postId}")
    public ResponseDto<PostShareResponseDto> postShare(@PathVariable Long postId,
                                                       @AuthenticationPrincipal User user) {
        PostShareResponseDto response = postService.updatePostShare(postId, user);
        return ResponseDto.of(response);
    }

    @PatchMapping("/{postId}")
    public ResponseDto<PostShareResponseDto> postUnshare(@PathVariable Long postId,
                                                         @AuthenticationPrincipal User user) {
        PostShareResponseDto response = postService.updatePostUnShare(postId, user);
        return ResponseDto.of(response);
    }

    @GetMapping("/{postId}")
    public ResponseDto<PostShareDetailResponseDto> getPostShare(@PathVariable Long postId,
                                                                @AuthenticationPrincipal User user) {
        PostShareDetailResponseDto response = postService.getPostDetail(postId, user);
        return ResponseDto.of(response);
    }

    @GetMapping("")
    public ResponseDto<PostBoardResponseDto> getPostBoard() {
        PostBoardResponseDto response = postService.getPostList();
        return ResponseDto.of(response);
    }
}
