package com.example.cap1.domain.comment.domain;

import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.global.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  //지연 로딩
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)  //지연 로딩
    @JoinColumn(name = "postId")
    private Post post;

    private String contents;
    private Float rating;
}
