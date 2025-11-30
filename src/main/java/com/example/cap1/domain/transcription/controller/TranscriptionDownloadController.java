package com.example.cap1.domain.transcription.controller;

import com.example.cap1.domain.user.domain.User;
import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/transcription/download")
@RequiredArgsConstructor
public class TranscriptionDownloadController {

    @Value("${file.transcription-dir:./uploads/transcription}")
    private String transcriptionDir;

    /**
     * 음원 분리 파일 다운로드
     * GET /api/transcription/download/{aiJobId}/separated/{instrument}
     */
    @GetMapping("/{aiJobId}/separated/{instrument}")
    public ResponseEntity<Resource> downloadSeparatedTrack(
            @PathVariable String aiJobId,
            @PathVariable String instrument
            // TODO: JWT 구현 후 @AuthenticationPrincipal User user 추가
    ) {
        log.info("음원 분리 파일 다운로드 요청 - aiJobId: {}, instrument: {}",
                aiJobId, instrument);

        // 악기 유효성 검증
        if (!isValidInstrument(instrument)) {
            throw new GeneralException(Code.INSTRUMENT_NOT_SUPPORTED);
        }

        try {
            // 파일 경로 구성
            Path filePath = Paths.get(transcriptionDir)
                    .resolve(aiJobId)
                    .resolve("separated")
                    .resolve(instrument + ".opus")
                    .normalize();

            log.debug("파일 경로: {}", filePath);

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("파일을 찾을 수 없거나 읽을 수 없음: {}", filePath);
                throw new GeneralException(Code.ENTITY_NOT_FOUND, "파일을 찾을 수 없습니다.");
            }

            // TODO: 권한 확인 (해당 aiJobId가 현재 사용자의 것인지)

            String filename = instrument + "_track.opus";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("파일 다운로드 실패", e);
            throw new GeneralException(Code.INTERNAL_ERROR, "파일 다운로드에 실패했습니다.");
        }
    }

    /**
     * MIDI 파일 다운로드
     * GET /api/transcription/download/{aiJobId}/midi
     */
    @GetMapping("/{aiJobId}/midi")
    public ResponseEntity<Resource> downloadMidi(
            @PathVariable String aiJobId,
            @AuthenticationPrincipal User user
    ) {
        log.info("MIDI 파일 다운로드 요청 - aiJobId: {}", aiJobId);

        try {
            Path filePath = Paths.get(transcriptionDir)
                    .resolve(aiJobId)
                    .resolve("transcription.mid")
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("MIDI 파일을 찾을 수 없음: {}", filePath);
                throw new GeneralException(Code.ENTITY_NOT_FOUND, "MIDI 파일을 찾을 수 없습니다.");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"transcription.mid\"")
                    .body(resource);

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("MIDI 파일 다운로드 실패", e);
            throw new GeneralException(Code.INTERNAL_ERROR, "MIDI 파일 다운로드에 실패했습니다.");
        }
    }

    /**
     * 코드 진행 파일 다운로드
     * GET /api/transcription/download/{aiJobId}/chords/{format}
     */
    @GetMapping("/{aiJobId}/chords/{format}")
    public ResponseEntity<Resource> downloadChords(
            @PathVariable String aiJobId,
            @PathVariable String format,  // "json" or "txt"
            @AuthenticationPrincipal User user
    ) {
        log.info("코드 진행 파일 다운로드 요청 - aiJobId: {}, format: {}", aiJobId, format);

        // 포맷 유효성 검증
        if (!format.equals("json") && !format.equals("txt")) {
            throw new GeneralException(Code.BAD_REQUEST, "지원하지 않는 파일 형식입니다. (json 또는 txt만 가능)");
        }

        try {
            String fileName = "chord_progression." + format;
            Path filePath = Paths.get(transcriptionDir)
                    .resolve(aiJobId)
                    .resolve(fileName)
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("코드 진행 파일을 찾을 수 없음: {}", filePath);
                throw new GeneralException(Code.ENTITY_NOT_FOUND, "코드 진행 파일을 찾을 수 없습니다.");
            }

            MediaType mediaType = "json".equals(format)
                    ? MediaType.APPLICATION_JSON
                    : MediaType.TEXT_PLAIN;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("코드 진행 파일 다운로드 실패", e);
            throw new GeneralException(Code.INTERNAL_ERROR, "코드 진행 파일 다운로드에 실패했습니다.");
        }
    }

    /**
     * 악기명 유효성 검증
     */
    private boolean isValidInstrument(String instrument) {
        return instrument.equals("guitar")
                || instrument.equals("bass")
                || instrument.equals("vocal")
                || instrument.equals("drums");
    }
}