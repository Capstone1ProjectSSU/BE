package com.example.cap1.domain.sheet.controller;

import com.example.cap1.domain.transcription.domain.JobType;
import com.example.cap1.domain.transcription.service.DifficultyService;
import com.example.cap1.global.response.ResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/difficulty")
@RequiredArgsConstructor
public class DifficultyController {
    private final DifficultyService difficultyService;

    @PostMapping("/easier")
    public ResponseDto<DifficultyService.DifficultyResponse> requestEasier(@RequestBody DifficultyRequest req) {
        return ResponseDto.of(difficultyService.requestDifficultyChange(1L, req.getSheetId(), JobType.EASIER));
    }
    @PostMapping("/harder")
    public ResponseDto<DifficultyService.DifficultyResponse> requestHarder(@RequestBody DifficultyRequest req) {
        return ResponseDto.of(difficultyService.requestDifficultyChange(1L, req.getSheetId(), JobType.HARDER));
    }
    @GetMapping("/job/{jobId}")
    public ResponseDto<DifficultyService.DifficultyResponse> getJobStatus(@PathVariable Long jobId) {
        return ResponseDto.of(difficultyService.getJobStatus(1L, jobId));
    }
    @Getter @NoArgsConstructor
    public static class DifficultyRequest { private Long sheetId; }
}