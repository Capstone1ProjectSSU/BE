package com.example.cap1.domain.post.service;

import com.example.cap1.domain.comment.converter.CommentConverter;
import com.example.cap1.domain.comment.dto.response.CommentListResponseDto;
import com.example.cap1.domain.post.converter.PostConverter;
import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.post.dto.response.PostShareDetailResponseDto;
import com.example.cap1.domain.post.dto.response.PostShareResponseDto;
import com.example.cap1.domain.post.repository.PostRepository;
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

    public PostShareResponseDto updatePostShare(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return null;
        } else{
            post.updateShare();
        }
        return PostConverter.toPostShareResponseDto(post);
    }

    public PostShareResponseDto updatePostUnShare(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return null;
        } else{
            post.updateUnShare();
        }
        return PostConverter.toPostShareResponseDto(post);
    }

    public PostShareDetailResponseDto getPostDetail(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);

        if (post == null) {
            return null;
        }

        // 엔티티 컬렉션을 DTO 리스트로 변환 (null-safe)
        List<CommentListResponseDto> commentList = Optional.ofNullable(post.getCommentList())
                .orElseGet(Collections::emptyList)
                .stream()
                // 필요하면 정렬: .sorted(Comparator.comparing(BaseEntity::getCreatedAt).reversed())
                .map(CommentConverter::toCommentListResponseDto)
                .collect(Collectors.toList());


        return PostConverter.toPostShareDetailResponseDto(post, commentList);
    }
}
