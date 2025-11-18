package com.example.cap1.domain.comment.service;

import com.example.cap1.domain.comment.converter.CommentConverter;
import com.example.cap1.domain.comment.domain.Comment;
import com.example.cap1.domain.comment.dto.request.CreateCommentRequestDto;
import com.example.cap1.domain.comment.dto.response.CreateCommentResponseDto;
import com.example.cap1.domain.comment.repository.CommentRepository;
import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.post.repository.PostRepository;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public CreateCommentResponseDto addComment(Long postId, CreateCommentRequestDto requestDto) {
        Post post = postRepository.findById(postId).orElseThrow(RuntimeException::new);
        Long userId = requestDto.getUserId();
        User user = userRepository.findById(userId).orElseThrow(RuntimeException::new);
        Comment comment = CommentConverter.toComment(user, post, requestDto);
        commentRepository.save(comment);
        updatePostRating(post);
        return CommentConverter.toCreateCommentResponseDto(comment);
    }

    private void updatePostRating(Post post) {
        // 해당 Post의 모든 댓글 평점을 가져옴
        Float avgRating = commentRepository.getAverageRatingByPost(post.getId());

        // null이면 댓글 없음 → 0.0 처리
        post.setRating(avgRating != null ? avgRating : 0.0f);
    }
}
