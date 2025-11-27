package com.example.cap1.domain.transcription.controller;

import com.example.cap1.domain.transcription.dto.request.TranscriptionRequest;
import com.example.cap1.domain.transcription.dto.response.TranscriptionResponse;
import com.example.cap1.domain.transcription.dto.response.TranscriptionStatusResponse;
import com.example.cap1.domain.transcription.service.TranscriptionService;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/transcription")
@RequiredArgsConstructor
public class TranscriptionController {

    private final TranscriptionService transcriptionService;

    /**
     * 악보 생성 요청 (E2E 파이프라인 시작)
     *
     * POST /api/transcription/request
     *
     * @param request 악보 생성 요청 정보
     * @return 생성된 작업 정보
     */
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseDto<TranscriptionResponse> requestTranscription(
            @RequestBody TranscriptionRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.info("악보 생성 요청 API 호출 - audioId: {}, instrument: {}",
                request.getAudioId(), request.getInstrument());

        Long userId = user.getId();

        TranscriptionResponse response = transcriptionService
                .requestTranscription(userId, request);

        return ResponseDto.of(response);
    }

    /**
     * 🆕 악보 생성 상태 조회
     *
     * GET /api/transcription/status/{jobId}
     *
     * @param jobId 작업 ID
     * @return 작업 상태 정보
     */
    @GetMapping("/status/{jobId}")
    public ResponseDto<TranscriptionStatusResponse> getTranscriptionStatus(
            @PathVariable Long jobId,
            @AuthenticationPrincipal User user
    ) {
        log.info("악보 생성 상태 조회 API 호출 - jobId: {}", jobId);

        Long userId = user.getId();

        TranscriptionStatusResponse response = transcriptionService
                .getTranscriptionStatus(jobId, userId);

        return ResponseDto.of(response);
    }
}