package com.example.cap1.domain.post.domain;

import com.example.cap1.domain.comment.domain.Comment;
import com.example.cap1.domain.sheet.domain.Sheet;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.global.database.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)   //지연 로딩
    @JoinColumn(name = "sheet_id")
    private Sheet sheet;

    @Setter
    @ColumnDefault("0.0")
    private Float rating;
    private Integer share;     // 공유 여부 1: 공개, 0: 비공개

    @ColumnDefault("0")
    private Long commentCount;      // 댓글 수

    @OneToMany(mappedBy = "post")
    private List<Comment> commentList = new ArrayList<>();

    public void updateShare(){
        this.share = 1;
    }

    public void updateUnShare(){
        this.share = 0;
    }

}
