package com.example.cap1.domain.comment.api;

import com.example.cap1.domain.comment.dto.request.CreateCommentRequestDto;
import com.example.cap1.domain.comment.dto.request.UpdateCommentRequestDto;
import com.example.cap1.domain.comment.dto.response.CreateCommentResponseDto;
import com.example.cap1.domain.comment.dto.response.DeleteCommentResponseDto;
import com.example.cap1.domain.comment.dto.response.UpdateCommentResponseDto;
import com.example.cap1.domain.comment.service.CommentService;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.hibernate.sql.Update;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/{postId}")
    public ResponseDto<CreateCommentResponseDto> createComment(@PathVariable Long postId,
                                                               @RequestBody CreateCommentRequestDto requestDto,
                                                               @AuthenticationPrincipal User user) {
        CreateCommentResponseDto result = commentService.addComment(postId, requestDto, user);
        return ResponseDto.of(result);
    }

    @DeleteMapping("/{commentId}")
    public ResponseDto<DeleteCommentResponseDto> deleteComment(@PathVariable Long commentId,
                                                               @AuthenticationPrincipal User user) {
        DeleteCommentResponseDto result = commentService.deleteComment(commentId, user);
        return ResponseDto.of(result);
    }

    @PatchMapping("/{commentId}")
    public ResponseDto<UpdateCommentResponseDto> updateComment(@PathVariable Long commentId,
                                                               @RequestBody UpdateCommentRequestDto request,
                                                               @AuthenticationPrincipal User user){
        UpdateCommentResponseDto result = commentService.updateComment(commentId, request, user);
        return ResponseDto.of(result);
    }
}
