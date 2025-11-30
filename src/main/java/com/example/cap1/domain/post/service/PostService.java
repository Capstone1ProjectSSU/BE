package com.example.cap1.domain.post.service;

import com.example.cap1.domain.comment.converter.CommentConverter;
import com.example.cap1.domain.comment.dto.response.CommentListResponseDto;
import com.example.cap1.domain.post.converter.PostConverter;
import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.post.dto.response.PostBoardResponseDto;
import com.example.cap1.domain.post.dto.response.PostListResponseDto;
import com.example.cap1.domain.post.dto.response.PostShareDetailResponseDto;
import com.example.cap1.domain.post.dto.response.PostShareResponseDto;
import com.example.cap1.domain.post.repository.PostRepository;
import com.example.cap1.domain.sheet.domain.Sheet;
import com.example.cap1.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public PostShareResponseDto updatePostShare(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));

        // 본인 확인
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인의 게시글만 공유할 수 있습니다");
        }

        post.updateShare();

        Sheet sheet = post.getSheet();
        if (sheet != null) {
            sheet.setShare(1);
        }

        return PostConverter.toPostShareResponseDto(post);
    }

    public PostShareResponseDto updatePostUnShare(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));

        // 본인 확인
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("본인의 게시글만 공유 취소할 수 있습니다");
        }

        post.updateUnShare();

        Sheet sheet = post.getSheet();
        if (sheet != null) {
            sheet.setShare(0);
        }

        return PostConverter.toPostShareResponseDto(post);
    }

    public PostShareDetailResponseDto getPostDetail(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다"));

        boolean isMyPost = post.getUser().getId().equals(user.getId());

        // 엔티티 컬렉션을 DTO 리스트로 변환 (null-safe)
        List<CommentListResponseDto> commentList = Optional.ofNullable(post.getCommentList())
                .orElseGet(Collections::emptyList)
                .stream()
                .map(comment -> CommentConverter.toCommentListResponseDto(comment, user))
                .collect(Collectors.toList());

        return PostConverter.toPostShareDetailResponseDto(post, commentList, isMyPost);
    }

    public PostBoardResponseDto getPostList() {
        List<PostListResponseDto> board = postRepository.findAllWithSheetOrderByCreatedAtDesc()
                .stream()
                .map(PostConverter::toPostListResponseDto)
                .collect(Collectors.toList());

        PostBoardResponseDto data = PostBoardResponseDto.builder()
                .board(board)
                .build();

        return data;
    }
}
