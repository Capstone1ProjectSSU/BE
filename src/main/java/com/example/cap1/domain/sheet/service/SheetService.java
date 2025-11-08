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

    /**
     * 사용자의 악보 목록을 조회합니다.
     */
    public SheetListResponse getSheetList(Long userId, SheetSearchRequest request) {
        log.info("악보 목록 조회 - userId: {}, keyword: {}, page: {}",
                userId, request.getKeyword(), request.getPage());

        Pageable pageable = createPageable(request);

        Page<Sheet> sheetPage = sheetRepository.findByUserIdWithFilters(
                userId,
                request.getKeyword(),
                request.getInstrument(),
                request.getDifficulty(),
                pageable
        );

        Page<SheetSummaryDto> dtoPage = sheetPage.map(SheetSummaryDto::from);

        log.info("악보 목록 조회 완료 - 총 {}개, {}페이지 중 {}페이지",
                dtoPage.getTotalElements(), dtoPage.getTotalPages(), dtoPage.getNumber() + 1);

        return SheetListResponse.from(dtoPage, request.getSort());
    }

    /**
     * 악보 상세 정보를 조회합니다.
     */
    public SheetDetailResponse getSheetDetail(Long musicId, Long userId) {
        log.info("악보 상세 조회 - musicId: {}, userId: {}", musicId, userId);

        Sheet sheet = sheetRepository.findById(musicId)
                .orElseThrow(() -> new GeneralException(Code.SHEET_NOT_FOUND));

        if (!sheet.getUserId().equals(userId)) {
            log.warn("악보 접근 권한 없음 - musicId: {}, requestUserId: {}, ownerUserId: {}",
                    musicId, userId, sheet.getUserId());
            throw new GeneralException(Code.SHEET_FORBIDDEN);
        }

        Audio audio = audioRepository.findById(sheet.getAudioId())
                .orElseThrow(() -> new GeneralException(Code.AUDIO_NOT_FOUND));

        log.info("악보 상세 조회 완료 - musicId: {}, title: {}", musicId, sheet.getTitle());

        return SheetDetailResponse.of(sheet, audio);
    }

    /**
     * 악보 정보를 수정합니다.
     *
     * @param musicId 악보 ID
     * @param userId 사용자 ID
     * @param request 수정할 정보
     * @return 수정된 악보 상세 정보
     */
    @Transactional
    public SheetDetailResponse updateSheet(Long musicId, Long userId, SheetUpdateRequest request) {
        log.info("악보 수정 요청 - musicId: {}, userId: {}", musicId, userId);

        // 1. 요청 데이터 검증
        request.validate();

        // 2. Sheet 조회
        Sheet sheet = sheetRepository.findById(musicId)
                .orElseThrow(() -> new GeneralException(Code.SHEET_NOT_FOUND));

        // 3. 권한 확인
        if (!sheet.getUserId().equals(userId)) {
            log.warn("악보 수정 권한 없음 - musicId: {}, requestUserId: {}, ownerUserId: {}",
                    musicId, userId, sheet.getUserId());
            throw new GeneralException(Code.SHEET_FORBIDDEN);
        }

        // 4. Sheet 정보 업데이트
        sheet.update(
                request.getTitle(),
                request.getArtist(),
                request.getInstrument(),
                request.getDifficulty(),
                request.getTuning(),
                request.getCapo(),
                request.getTempo(),
                request.getKey()
        );

        // 5. 변경사항 저장 (더티 체킹으로 자동 저장됨)
        Sheet updatedSheet = sheetRepository.save(sheet);

        // 6. Audio 조회
        Audio audio = audioRepository.findById(updatedSheet.getAudioId())
                .orElseThrow(() -> new GeneralException(Code.AUDIO_NOT_FOUND));

        log.info("악보 수정 완료 - musicId: {}, title: {}", musicId, updatedSheet.getTitle());

        return SheetDetailResponse.of(updatedSheet, audio);
    }

    /**
     * 악보를 삭제합니다.
     */
    @Transactional
    public void deleteSheet(Long musicId, Long userId) {
        log.info("악보 삭제 요청 - musicId: {}, userId: {}", musicId, userId);

        Sheet sheet = sheetRepository.findById(musicId)
                .orElseThrow(() -> new GeneralException(Code.SHEET_NOT_FOUND));

        if (!sheet.getUserId().equals(userId)) {
            log.warn("악보 삭제 권한 없음 - musicId: {}, requestUserId: {}, ownerUserId: {}",
                    musicId, userId, sheet.getUserId());
            throw new GeneralException(Code.SHEET_FORBIDDEN);
        }

        sheetRepository.delete(sheet);

        log.info("악보 삭제 완료 - musicId: {}, title: {}", musicId, sheet.getTitle());
    }

    private Pageable createPageable(SheetSearchRequest request) {
        int page = Math.max(0, request.getPage());
        int size = Math.min(100, Math.max(1, request.getSize()));
        Sort sort = parseSort(request.getSort());

        return PageRequest.of(page, size, sort);
    }

    private Sort parseSort(String sortStr) {
        if (sortStr == null || sortStr.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sortStr.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.DESC;

        if (parts.length > 1) {
            String dirStr = parts[1].trim().toUpperCase();
            if ("ASC".equals(dirStr)) {
                direction = Sort.Direction.ASC;
            }
        }

        if (!isValidSortProperty(property)) {
            log.warn("유효하지 않은 정렬 필드: {}. 기본값(createdAt) 사용", property);
            property = "createdAt";
        }

        return Sort.by(direction, property);
    }

    private boolean isValidSortProperty(String property) {
        return property.equals("createdAt")
                || property.equals("updatedAt")
                || property.equals("title")
                || property.equals("artist");
    }
}