package com.example.cap1.domain.transcription.service;

import com.example.cap1.domain.post.converter.PostConverter;
import com.example.cap1.domain.post.domain.Post;
import com.example.cap1.domain.post.repository.PostRepository;
import com.example.cap1.domain.sheet.domain.Difficulty;
import com.example.cap1.domain.sheet.domain.Sheet;
import com.example.cap1.domain.sheet.repository.SheetRepository;
import com.example.cap1.domain.transcription.client.AiServerClient;
import com.example.cap1.domain.transcription.domain.JobType;
import com.example.cap1.domain.transcription.domain.ProgressStage;
import com.example.cap1.domain.transcription.domain.TranscriptionJob;
import com.example.cap1.domain.transcription.dto.ai.AiEnqueueResponse;
import com.example.cap1.domain.transcription.dto.ai.AiResultResponse;
import com.example.cap1.domain.transcription.dto.response.TranscriptionResponse;
import com.example.cap1.domain.transcription.repository.TranscriptionJobRepository;
import com.example.cap1.domain.user.domain.User;
import com.example.cap1.domain.user.repository.UserRepository;
import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DifficultyService {
    private final TranscriptionJobRepository jobRepository;
    private final SheetRepository sheetRepository;
    private final AiServerClient aiServerClient;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public TranscriptionResponse requestDifficultyChange(Long userId, Long sheetId, JobType jobType) {
        Sheet originSheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new GeneralException(Code.SHEET_NOT_FOUND));

        // 원본 시트의 악기 정보를 계승하여 Job 생성
        TranscriptionJob job = TranscriptionJob.createDifficultyJob(
                userId,
                originSheet.getAudioId(),
                sheetId,
                originSheet.getInstrument(),
                jobType
        );
        jobRepository.save(job);

        AiEnqueueResponse res = aiServerClient.enqueueDifficultyTask(originSheet.getSheetDataUrl(), jobType);

        job.updateAiJobId(res.getJobId());
        job.updateStatus(ProgressStage.PROCESSING);

        return TranscriptionResponse.from(job);
    }

    // 스케줄러에 의해 호출되는 완료 처리 메서드
    public void completeDifficultyJob(TranscriptionJob job, AiResultResponse result) {
        Long originSheetId = job.getSheetId();
        Sheet originSheet = sheetRepository.findById(originSheetId)
                .orElseThrow(() -> new RuntimeException("Original sheet not found: " + originSheetId));

        Difficulty newDifficulty = (job.getJobType() == JobType.EASIER) ? Difficulty.EASY : Difficulty.HARD;
        String titleSuffix = (job.getJobType() == JobType.EASIER) ? " (Easy)" : " (Hard)";

        // 다운로드 경로 생성
        String downloadPath = "/api/transcription/download/" + job.getAiJobId() + "/chords/json";

        // 1. Sheet 생성
        Sheet newSheet = Sheet.builder()
                .userId(job.getUserId())
                .audioId(job.getAudioId())
                .title(originSheet.getTitle() + titleSuffix)
                .artist(originSheet.getArtist())
                .instrument(originSheet.getInstrument())
                .difficulty(newDifficulty)
                .sheetDataUrl(downloadPath)
                .key(originSheet.getKey()) // 원본 키 유지 또는 결과에서 파싱 가능
                .build();

        Sheet savedSheet = sheetRepository.save(newSheet);

        // 2. Post 생성 (내 라이브러리 노출용)
        User user = userRepository.findById(savedSheet.getUserId())
                .orElseThrow(() -> new GeneralException(Code.USER_NOT_FOUND));
        Post post = PostConverter.toPost(savedSheet, user);
        postRepository.save(post);

        // Job에 결과 Sheet ID 업데이트
        job.updateSheetId(savedSheet.getId());
    }
}