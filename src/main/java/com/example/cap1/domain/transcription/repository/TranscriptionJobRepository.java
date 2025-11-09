package com.example.cap1.domain.transcription.repository;

import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranscriptionJobRepository extends JpaRepository<TranscriptionJob, Long> {

    /**
     * 사용자 ID와 음원 ID로 작업 조회
     */
    Optional<TranscriptionJob> findByUserIdAndAudioId(Long userId, Long audioId);

    /**
     * 특정 상태의 작업 목록 조회 (폴링용)
     */
    List<TranscriptionJob> findByProgressStage(ProgressStage progressStage);

    /**
     * 사용자별 작업 목록 조회
     */
    List<TranscriptionJob> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 처리 중인 작업이 있는지 확인
     */
    boolean existsByAudioIdAndProgressStageIn(Long audioId, List<ProgressStage> stages);
}