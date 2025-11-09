package com.example.cap1.domain.transcription.controller;

import com.example.cap1.domain.transcription.dto.request.TranscriptionRequest;
import com.example.cap1.domain.transcription.dto.response.TranscriptionResponse;
import com.example.cap1.domain.transcription.service.TranscriptionService;
import com.example.cap1.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
            @RequestBody TranscriptionRequest request
            // TODO: JWT 구현 후 @AuthenticationPrincipal User user 추가
    ) {
        log.info("악보 생성 요청 API 호출 - audioId: {}, instrument: {}",
                request.getAudioId(), request.getInstrument());

        Long userId = 1L; // TODO: JWT에서 추출

        TranscriptionResponse response = transcriptionService
                .requestTranscription(userId, request);

        return ResponseDto.of(response);
    }
}