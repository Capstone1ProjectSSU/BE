package com.example.cap1.domain.sheet.repository;

import com.example.cap1.domain.sheet.domain.Difficulty;
import com.example.cap1.domain.sheet.domain.Sheet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SheetRepository extends JpaRepository<Sheet, Long> {

    /**
     * 사용자별 악보 목록 조회 (검색, 필터링 포함)
     */
    @Query("SELECT s FROM Sheet s WHERE s.userId = :userId " +
            "AND (:keyword IS NULL OR s.title LIKE %:keyword% OR s.artist LIKE %:keyword%) " +
            "AND (:instrument IS NULL OR s.instrument = :instrument) " +
            "AND (:difficulty IS NULL OR s.difficulty = :difficulty)")
    Page<Sheet> findByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("instrument") String instrument,
            @Param("difficulty") Difficulty difficulty,
            Pageable pageable
    );

    /**
     * 사용자별 악보 개수 조회
     */
    long countByUserId(Long userId);
}