package com.example.cap1.domain.audio.repository;

import com.example.cap1.domain.audio.domain.Audio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AudioRepository extends JpaRepository<Audio, Long> {

    List<Audio> findByUserId(Long userId);

    boolean existsByUserIdAndTitle(Long userId, String title);
}