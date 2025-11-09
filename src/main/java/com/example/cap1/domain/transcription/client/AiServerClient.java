package com.example.cap1.domain.transcription.client;

import com.example.cap1.domain.transcription.dto.ai.AiEnqueueResponse;
import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient {

    @Value("${ai.server.base-url}")
    private String aiServerBaseUrl;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${ai.server.mock-mode:true}")  // 🆕 기본값 true (Mock 모드)
    private boolean mockMode;

    private final RestTemplate restTemplate;

    /**
     * E2E Task 등록 (음원 분리 + MIDI 변환 + 코드 인지)
     */
    public AiEnqueueResponse enqueueE2ETask(String audioFilePath, String instrument) {

        // 🆕 Mock 모드일 경우 가짜 응답 반환
        if (mockMode) {
            log.warn("⚠️ AI 서버 Mock 모드 활성화 - 실제 AI 서버에 요청하지 않음");
            return createMockResponse();
        }

        // 실제 AI 서버 호출
        return callRealAiServer(audioFilePath, instrument);
    }

    /**
     * 🆕 Mock 응답 생성
     */
    private AiEnqueueResponse createMockResponse() {
        String mockJobId = "mock-ai-job-" + System.currentTimeMillis();
        String queuedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        log.info("✅ Mock AI Job 생성 - jobId: {}", mockJobId);

        return new AiEnqueueResponse(
                mockJobId,
                "queued",
                queuedAt
        );
    }

    /**
     * 실제 AI 서버 호출
     */
    private AiEnqueueResponse callRealAiServer(String audioFilePath, String instrument) {
        String url = aiServerBaseUrl + "/tasks/e2e-base/enqueue";

        try {
            // Multipart 요청 생성
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 파일 경로를 실제 파일로 변환
            String fullPath = uploadDir + audioFilePath.replace("/uploads/audio/", "/");
            File audioFile = new File(fullPath);

            if (!audioFile.exists()) {
                log.error("음원 파일을 찾을 수 없음: {}", fullPath);
                throw new GeneralException(Code.AUDIO_NOT_FOUND);
            }

            body.add("audioFile", new FileSystemResource(audioFile));
            body.add("instrument", instrument);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            log.info("AI 서버에 E2E Task 등록 요청 - URL: {}, instrument: {}",
                    url, instrument);

            ResponseEntity<AiEnqueueResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    AiEnqueueResponse.class
            );

            AiEnqueueResponse result = response.getBody();

            log.info("AI 서버 E2E Task 등록 성공 - aiJobId: {}", result.getJobId());

            return result;

        } catch (RestClientException e) {
            log.error("AI 서버 E2E Task 등록 실패", e);
            throw new GeneralException(Code.AI_SERVER_ERROR,
                    "AI 서버와의 통신에 실패했습니다: " + e.getMessage());
        }
    }
}