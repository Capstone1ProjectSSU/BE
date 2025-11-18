package com.example.cap1.domain.comment.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class CreateCommentRequestDto {
//    private Long userId;
    private Float rating;
    private String content;

}
