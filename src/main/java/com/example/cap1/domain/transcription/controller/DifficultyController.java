package com.example.cap1.domain.transcription.controller;

import com.example.cap1.domain.transcription.domain.JobType;
import com.example.cap1.domain.transcription.dto.request.DifficultyRequest;
import com.example.cap1.domain.transcription.dto.response.TranscriptionResponse;
import com.example.cap1.domain.transcription.service.DifficultyService;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.global.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/difficulty")
@RequiredArgsConstructor
public class DifficultyController {

    private final DifficultyService difficultyService;

    @PostMapping("/easier")
    public ResponseDto<TranscriptionResponse> requestEasier(
            @RequestBody DifficultyRequest req,
            @AuthenticationPrincipal User user // 🚀 추가
    ) {
        return ResponseDto.of(difficultyService.requestDifficultyChange(user.getId(), req.getSheetId(), JobType.EASIER));
    }

    @PostMapping("/harder")
    public ResponseDto<TranscriptionResponse> requestHarder(
            @RequestBody DifficultyRequest req,
            @AuthenticationPrincipal User user // 🚀 추가
    ) {
        return ResponseDto.of(difficultyService.requestDifficultyChange(user.getId(), req.getSheetId(), JobType.HARDER));
    }
}