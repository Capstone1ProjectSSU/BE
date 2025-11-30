package com.example.cap1.domain.sheet.service;

import com.example.cap1.domain.audio.domain.Audio;
import com.example.cap1.domain.audio.repository.AudioRepository;
import com.example.cap1.domain.sheet.domain.Sheet;
import com.example.cap1.domain.sheet.dto.request.SheetSearchRequest;
import com.example.cap1.domain.sheet.dto.request.SheetUpdateRequest;
import com.example.cap1.domain.sheet.dto.response.SheetDetailResponse;
import com.example.cap1.domain.sheet.dto.response.SheetListResponse;
import com.example.cap1.domain.sheet.dto.response.SheetSummaryDto;
import com.example.cap1.domain.sheet.repository.SheetRepository;
import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SheetService {

    private final SheetRepository sheetRepository;
    private final AudioRepository audioRepository;

    public SheetListResponse getSheetList(Long userId, SheetSearchRequest request) {
        Pageable pageable = createPageable(request);
        Page<Sheet> sheetPage = sheetRepository.findByUserIdWithFilters(
                userId,
                request.getKeyword(),
                request.getInstrument(),
                request.getDifficulty(),
                pageable
        );
        Page<SheetSummaryDto> dtoPage = sheetPage.map(SheetSummaryDto::from);
        return SheetListResponse.from(dtoPage, request.getSort());
    }

    public SheetDetailResponse getSheetDetail(Long sheetId, Long userId) {
        Sheet sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new GeneralException(Code.SHEET_NOT_FOUND));

        if (!sheet.getUserId().equals(userId)) {
            throw new GeneralException(Code.SHEET_FORBIDDEN);
        }

        Audio audio = audioRepository.findById(sheet.getAudioId())
                .orElseThrow(() -> new GeneralException(Code.AUDIO_NOT_FOUND));

        return SheetDetailResponse.of(sheet, audio);
    }

    @Transactional
    public SheetDetailResponse updateSheet(Long sheetId, Long userId, SheetUpdateRequest request) {
        request.validate();

        Sheet sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new GeneralException(Code.SHEET_NOT_FOUND));

        if (!sheet.getUserId().equals(userId)) {
            throw new GeneralException(Code.SHEET_FORBIDDEN);
        }

        // 🆕 수정: 변경된 매개변수만 전달
        sheet.update(
                request.getTitle(),
                request.getArtist(),
                request.getInstrument(),
                request.getDifficulty(),
                request.getKey()
        );

        Sheet updatedSheet = sheetRepository.save(sheet);
        Audio audio = audioRepository.findById(updatedSheet.getAudioId())
                .orElseThrow(() -> new GeneralException(Code.AUDIO_NOT_FOUND));

        return SheetDetailResponse.of(updatedSheet, audio);
    }

    @Transactional
    public void deleteSheet(Long sheetId, Long userId) {
        Sheet sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new GeneralException(Code.SHEET_NOT_FOUND));

        if (!sheet.getUserId().equals(userId)) {
            throw new GeneralException(Code.SHEET_FORBIDDEN);
        }
        sheetRepository.delete(sheet);
    }

    private Pageable createPageable(SheetSearchRequest request) {
        int page = Math.max(0, request.getPage());
        int size = Math.min(100, Math.max(1, request.getSize()));
        Sort sort = parseSort(request.getSort());
        return PageRequest.of(page, size, sort);
    }

    private Sort parseSort(String sortStr) {
        // (기존 로직 유지)
        if (sortStr == null || sortStr.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sortStr.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;
        if (parts.length > 1 && "ASC".equalsIgnoreCase(parts[1].trim())) {
            direction = Sort.Direction.ASC;
        }
        return Sort.by(direction, "createdAt"); // 안전하게 기본값
    }
}