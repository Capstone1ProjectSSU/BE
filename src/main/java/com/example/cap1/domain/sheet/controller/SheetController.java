package com.example.cap1.domain.sheet.controller;

import com.example.cap1.domain.sheet.dto.request.SheetSearchRequest;
import com.example.cap1.domain.sheet.dto.request.SheetUpdateRequest;
import com.example.cap1.domain.sheet.dto.response.SheetDetailResponse;
import com.example.cap1.domain.sheet.dto.response.SheetListResponse;
import com.example.cap1.domain.sheet.service.SheetService;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.global.response.ApiResponse;
import com.example.cap1.global.response.Code;
import com.example.cap1.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sheets")
@RequiredArgsConstructor
public class SheetController {

    private final SheetService sheetService;

    /**
     * 악보 목록을 조회합니다.
     */
    @GetMapping
    public ResponseDto<SheetListResponse> getSheetList(
            @ModelAttribute SheetSearchRequest request,
            @AuthenticationPrincipal User user // 🚀 추가
    ) {
        log.info("악보 목록 조회 API 호출 - keyword: {}, page: {}",
                request.getKeyword(), request.getPage());

        Long userId = user.getId(); // 🚀 수정 (하드코딩 1L 제거)

        SheetListResponse response = sheetService.getSheetList(userId, request);

        return ResponseDto.of(response);
    }

    /**
     * 악보 상세 정보를 조회합니다.
     */
    @GetMapping("/{musicId}")
    public ResponseDto<SheetDetailResponse> getSheetDetail(
            @PathVariable Long musicId,
            @AuthenticationPrincipal User user // 🚀 추가
    ) {
        log.info("악보 상세 조회 API 호출 - musicId: {}", musicId);

        Long userId = user.getId(); // 🚀 수정 (하드코딩 1L 제거)

        SheetDetailResponse response = sheetService.getSheetDetail(musicId, userId);

        return ResponseDto.of(response);
    }

    /**
     * 악보 정보를 수정합니다.
     */
    @PutMapping("/{musicId}")
    public ResponseDto<SheetDetailResponse> updateSheet(
            @PathVariable Long musicId,
            @RequestBody SheetUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.info("악보 수정 API 호출 - musicId: {}", musicId);

        Long userId = user.getId();

        SheetDetailResponse response = sheetService.updateSheet(musicId, userId, request);

        return ResponseDto.of(response);
    }

    /**
     * 악보를 삭제합니다.
     */
    @DeleteMapping("/{musicId}")
    public ApiResponse deleteSheet(
            @PathVariable Long musicId,
            @AuthenticationPrincipal User user
    ) {
        log.info("악보 삭제 API 호출 - musicId: {}", musicId);

        Long userId = user.getId();

        sheetService.deleteSheet(musicId, userId);

        return ApiResponse.of(true, Code.OK, "악보가 삭제되었습니다.");
    }
}