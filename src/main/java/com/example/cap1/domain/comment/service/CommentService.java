package com.example.cap1.domain.comment.service;

import com.example.cap1.domain.comment.converter.CommentConverter;
import com.example.cap1.domain.comment.domain.Comment;
import com.example.cap1.domain.comment.dto.request.CreateCommentRequestDto;
import com.example.cap1.domain.comment.dto.request.UpdateCommentRequestDto;
import com.example.cap1.domain.comment.dto.response.CreateCommentResponseDto;
import com.example.cap1.domain.comment.dto.response.DeleteCommentResponseDto;
import com.example.cap1.domain.comment.dto.response.UpdateCommentResponseDto;
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

    private void updatePostRating(Post post) {
        // 해당 Post의 모든 댓글 평점을 가져옴
        Float avgRating = commentRepository.getAverageRatingByPost(post.getId());

        // null이면 댓글 없음 → 0.0 처리
        post.setRating(avgRating != null ? avgRating : 0.0f);
    }

    public CreateCommentResponseDto addComment(Long postId, CreateCommentRequestDto requestDto, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));

        // 토큰에서 가져온 user 사용
        Comment comment = CommentConverter.toComment(user, post, requestDto);
        commentRepository.save(comment);

        post.incrementCommentCount();
        updatePostRating(post);

        return CommentConverter.toCreateCommentResponseDto(comment);
    }

    public DeleteCommentResponseDto deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));

        // 작성자 본인 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인이 작성한 댓글만 삭제할 수 있습니다");
        }

        Post post = comment.getPost();
        commentRepository.delete(comment);

        // 댓글 수 감소
        post.decrementCommentCount();

        // 평균 평점 재계산
        Long cnt = commentRepository.countByPostId(post.getId());
        if (cnt == 0L) {
            post.setRating(0.0f);
            post.setCommentCount(0L);
        } else {
            Float avg = commentRepository.getAverageRatingByPost(post.getId());
            post.setRating(avg != null ? avg : 0.0f);
            post.setCommentCount(cnt);
        }

        return CommentConverter.toDeleteCommentResponseDto(comment);
    }

    public UpdateCommentResponseDto updateComment(Long commentId, UpdateCommentRequestDto request, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다"));

        // 작성자 본인 확인
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인이 작성한 댓글만 수정할 수 있습니다");
        }

        comment.setContents(request.getContent());
        comment.setRating(request.getRating());

        Comment updatedComment = commentRepository.save(comment);

        // 평균 평점 재계산
        Post post = comment.getPost();
        updatePostRating(post);

        return CommentConverter.toUpdateCommentResponseDto(updatedComment);
    }
}
