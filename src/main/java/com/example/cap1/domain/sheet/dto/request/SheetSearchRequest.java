package com.example.cap1.domain.sheet.dto.request;

import com.example.cap1.domain.sheet.domain.Difficulty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SheetSearchRequest {

    private String keyword;        // 검색 키워드 (제목 또는 아티스트)
    private String instrument;     // 악기 필터
    private Difficulty difficulty; // 난이도 필터

    private Integer page = 0;      // 페이지 번호 (0부터 시작)
    private Integer size = 20;     // 페이지 크기 (기본 20)
    private String sort = "createdAt,desc";  // 정렬 (기본: 최신순)
}