package com.example.cap1.domain.audio.controller;

import com.example.cap1.domain.audio.dto.request.AudioUploadRequest;
import com.example.cap1.domain.audio.dto.response.AudioUploadResponse;
import com.example.cap1.domain.audio.service.AudioService;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.domain.user.repository.UserRepository;
import com.example.cap1.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/audio")
@RequiredArgsConstructor
public class AudioController {

    private final AudioService audioService;

    /**
     * 음원 파일을 업로드합니다.
     *
     * POST /api/audio/upload
     * Content-Type: multipart/form-data
     *
     * @param request 업로드 요청 (파일 + 메타데이터)
     * @return 업로드된 음원 정보
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseDto<AudioUploadResponse> uploadAudio(
            @ModelAttribute AudioUploadRequest request,
            @AuthenticationPrincipal User user
    ) {
        log.info("음원 업로드 API 호출 - title: {}", request.getSongTitle());

        Long userId = user.getId();

        AudioUploadResponse response = audioService.uploadAudio(userId, request);

        return ResponseDto.of(response);
    }
}