package com.example.cap1.domain.sheet.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SheetListResponse {

    private List<SheetSummaryDto> content;
    private PageableInfo pageable;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageableInfo {
        private int page;
        private int size;
        private String sort;
    }

    public static SheetListResponse from(Page<SheetSummaryDto> page, String sort) {
        return SheetListResponse.builder()
                .content(page.getContent())
                .pageable(PageableInfo.builder()
                        .page(page.getNumber())
                        .size(page.getSize())
                        .sort(sort)
                        .build())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}