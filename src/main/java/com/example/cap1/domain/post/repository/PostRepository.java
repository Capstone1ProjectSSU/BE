package com.example.cap1.domain.post.repository;

import com.example.cap1.domain.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p join fetch p.sheet s order by p.createdAt desc")
    List<Post> findAllWithSheetOrderByCreatedAtDesc();

}
