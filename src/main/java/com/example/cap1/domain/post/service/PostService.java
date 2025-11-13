package com.example.cap1.domain.post.service;

import com.example.cap1.domain.post.converter.PostConverter;
import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.post.dto.response.PostShareResponseDto;
import com.example.cap1.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
