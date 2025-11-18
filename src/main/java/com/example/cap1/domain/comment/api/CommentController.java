package com.example.cap1.domain.comment.api;

import com.example.cap1.domain.comment.dto.request.CreateCommentRequestDto;
import com.example.cap1.domain.comment.dto.response.CreateCommentResponseDto;
import com.example.cap1.domain.comment.service.CommentService;
import com.example.cap1.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}")
    public ResponseDto<CreateCommentResponseDto> createComment(@PathVariable Long postId,
                                                               @RequestBody CreateCommentRequestDto requestDto) {
        CreateCommentResponseDto result = commentService.addComment(postId, requestDto);
        return ResponseDto.of(result);
    }


}
