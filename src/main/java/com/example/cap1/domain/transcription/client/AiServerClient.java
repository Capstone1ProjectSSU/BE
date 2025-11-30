package com.example.cap1.domain.transcription.client;

import com.example.cap1.domain.transcription.domain.JobType;
import com.example.cap1.domain.transcription.dto.ai.AiEnqueueResponse;
import com.example.cap1.domain.transcription.dto.ai.AiResultResponse;
import com.example.cap1.domain.transcription.dto.ai.AiStatusResponse;
import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiServerClient {

    @Value("${ai.server.base-url}")
    private String aiServerBaseUrl;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.transcription-dir:./uploads/transcription}")
    private String transcriptionDir;

    // 🚀 수정 1: 기본값을 false로 변경하여 배포 시 실수 방지
    @Value("${ai.server.mock-mode:false}")
    private boolean mockMode;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // --- Enqueue Methods ---

    public AiEnqueueResponse enqueueE2ETask(String audioFilePath, String instrument) {
        if (mockMode) {
            log.warn("⚠️ AI 서버 Mock 모드 활성화");
            return createMockEnqueueResponse();
        }
        return callRealAiServer(audioFilePath, instrument);
    }

    public AiEnqueueResponse enqueueDifficultyTask(String sheetDataUrl, JobType jobType) {
        if (mockMode) {
            return createMockEnqueueResponse();
        }

        String endpoint = (jobType == JobType.EASIER)
                ? "/tasks/easier-chord-recommendation/enqueue"
                : "/tasks/chord-complexification/enqueue";
        String url = aiServerBaseUrl + endpoint;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            File jsonFile = resolveFileFromUrl(sheetDataUrl);
            if (jsonFile == null || !jsonFile.exists()) {
                throw new GeneralException(Code.ENTITY_NOT_FOUND, "원본 악보 파일을 찾을 수 없습니다.");
            }

            body.add("chord_file", new FileSystemResource(jsonFile));

            // 🚀 수정 2: 작업 타입에 따라 필요한 파라미터 추가
            if (jobType == JobType.EASIER) {
                // 쉬운 모드: 악기 지정 (기존 로직)
                body.add("target_instrument", "guitar");
            } else if (jobType == JobType.HARDER) {
                // 🚀 어려운 모드: target_style 필수 파라미터 추가
                // AI 서버 에러 해결을 위해 추가함. 유효한 값은 AI 팀에 확인 필요 (예: "jazz", "standard" 등)
                body.add("target_style", "jazz");
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<AiEnqueueResponse> response = restTemplate.postForEntity(url, requestEntity, AiEnqueueResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI 서버 에러 (Difficulty Enqueue): {}", e.getResponseBodyAsString());
            throw new GeneralException(Code.AI_SERVER_ERROR, "AI 서버 에러: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("AI 서버 요청 실패", e);
            throw new GeneralException(Code.AI_SERVER_ERROR, "연결 실패: " + e.getMessage());
        }
    }

    // --- Status & Result Methods ---

    public AiStatusResponse getTaskStatus(String aiJobId, JobType jobType) {
        if (mockMode) return createMockStatusResponse(aiJobId);

        String taskPath = getTaskPath(jobType);
        String url = aiServerBaseUrl + "/tasks/" + taskPath + "/status/" + aiJobId;

        try {
            return restTemplate.getForEntity(url, AiStatusResponse.class).getBody();
        } catch (Exception e) {
            log.error("상태 조회 실패: {}", e.getMessage());
            throw new GeneralException(Code.AI_SERVER_ERROR, "상태 조회 실패");
        }
    }

    public AiResultResponse getTaskResult(String aiJobId, JobType jobType) {
        if (mockMode) return createMockResultResponse(aiJobId);

        String taskPath = getTaskPath(jobType);
        String url = aiServerBaseUrl + "/tasks/" + taskPath + "/result/" + aiJobId;

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String jsonBody = response.getBody();
            log.info("🔍 AI Server Result JSON [JobId: {}]: {}", aiJobId, jsonBody);
            return objectMapper.readValue(jsonBody, AiResultResponse.class);
        } catch (Exception e) {
            log.error("결과 조회 실패: {}", e.getMessage());
            throw new GeneralException(Code.AI_SERVER_ERROR, "결과 조회 실패: " + e.getMessage());
        }
    }

    public AiStatusResponse getTaskStatus(String aiJobId) {
        return getTaskStatus(aiJobId, JobType.TRANSCRIPTION);
    }
    public AiResultResponse getTaskResult(String aiJobId) {
        return getTaskResult(aiJobId, JobType.TRANSCRIPTION);
    }

    private String getTaskPath(JobType jobType) {
        if (jobType == null) return "e2e-base-ready";
        switch (jobType) {
            case EASIER: return "easier-chord-recommendation";
            case HARDER: return "chord-complexification";
            default: return "e2e-base-ready";
        }
    }

    // --- File Utils ---

    private File resolveFileFromUrl(String url) {
        try {
            if (url == null) return null;
            // url 예시: /api/transcription/download/{aiJobId}/chords/json
            // parts[0]='', parts[1]='api', parts[2]='transcription', parts[3]='download', parts[4]='{aiJobId}'
            String[] parts = url.split("/");
            if (parts.length < 5) return null;
            String aiJobId = parts[4];
            return Paths.get(transcriptionDir, aiJobId, "chord_progression.json").toFile();
        } catch (Exception e) {
            log.error("URL 파싱 실패: {}", url);
            return null;
        }
    }

    public void downloadAllFiles(String aiJobId, AiResultResponse result) {
        try {
            Path baseDir = Paths.get(transcriptionDir).resolve(aiJobId);
            Files.createDirectories(baseDir);

            JsonNode separatedNode = result.getSeparatedAudioUrl();
            if (separatedNode != null && !separatedNode.isNull()) {
                if (separatedNode.isTextual()) {
                    String url = separatedNode.asText();
                    String fileName = "separated_audio.wav";
                    if (url.endsWith(".opus")) fileName = "separated_audio.opus";
                    downloadFile(url, baseDir.resolve(fileName));
                } else if (separatedNode.isObject()) {
                    Path separatedDir = baseDir.resolve("separated");
                    Files.createDirectories(separatedDir);
                    downloadIfText(separatedNode.get("guitar"), separatedDir.resolve("guitar.opus"));
                    downloadIfText(separatedNode.get("bass"), separatedDir.resolve("bass.opus"));
                    downloadIfText(separatedNode.get("vocal"), separatedDir.resolve("vocal.opus"));
                    downloadIfText(separatedNode.get("drums"), separatedDir.resolve("drums.opus"));
                }
            }

            if (result.getTranscriptionUrl() != null) {
                downloadFile(result.getTranscriptionUrl(), baseDir.resolve("transcription.mid"));
            }
            if (result.getChordProgressionUrl() != null) {
                downloadFile(result.getChordProgressionUrl(), baseDir.resolve("chord_progression.json"));
            }
        } catch (IOException e) {
            log.error("다운로드 실패", e);
            throw new GeneralException(Code.INTERNAL_ERROR);
        }
    }

    private void downloadIfText(JsonNode node, Path path) throws IOException {
        if (node != null && node.isTextual()) {
            downloadFile(node.asText(), path);
        }
    }

    public void downloadChordOnly(String aiJobId, AiResultResponse result, JobType jobType) {
        try {
            Path baseDir = Paths.get(transcriptionDir).resolve(aiJobId);
            Files.createDirectories(baseDir);
            String targetUrl = (jobType == JobType.EASIER) ? result.getEasierChordProgressionUrl() : result.getComplexifiedChordProgressionUrl();

            if (targetUrl != null) {
                downloadFile(targetUrl, baseDir.resolve("chord_progression.json"));
            }
        } catch (IOException e) {
            log.error("코드 다운로드 실패", e);
            throw new GeneralException(Code.INTERNAL_ERROR);
        }
    }

    private void downloadFile(String urlPath, Path localPath) throws IOException {
        if (mockMode) {
            if (!Files.exists(localPath)) Files.createFile(localPath);
            return;
        }
        try {
            String fullUrl = urlPath.startsWith("http") ? urlPath : aiServerBaseUrl + urlPath;
            ResponseEntity<byte[]> response = restTemplate.getForEntity(fullUrl, byte[].class);
            if (response.getBody() != null) Files.write(localPath, response.getBody());
        } catch (Exception e) {
            throw new IOException("Download failed: " + urlPath);
        }
    }

    private AiEnqueueResponse callRealAiServer(String audioFilePath, String instrument) {
        String url = aiServerBaseUrl + "/tasks/e2e-base-ready/enqueue";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            String relativePath = audioFilePath.startsWith("/") ? audioFilePath.substring(1) : audioFilePath;
            Path filePath = Paths.get(relativePath).toAbsolutePath();
            File audioFile = filePath.toFile();

            if (!audioFile.exists()) {
                log.error("오디오 파일 없음: {}", filePath);
                throw new GeneralException(Code.AUDIO_NOT_FOUND);
            }

            body.add("audio_file", new FileSystemResource(audioFile));
            body.add("instrument", instrument);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.info("AI 서버 E2E 요청 전송: {}", url);
            ResponseEntity<AiEnqueueResponse> response = restTemplate.postForEntity(url, requestEntity, AiEnqueueResponse.class);

            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("AI 서버 응답 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new GeneralException(Code.AI_SERVER_ERROR, "AI 응답: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("AI 서버 연결 실패", e);
            throw new GeneralException(Code.AI_SERVER_ERROR, "연결 실패: " + e.getMessage());
        }
    }

    private AiEnqueueResponse createMockEnqueueResponse() {
        return new AiEnqueueResponse("mock-job-" + System.currentTimeMillis(), "queued", LocalDateTime.now().toString());
    }
    private AiStatusResponse createMockStatusResponse(String aiJobId) {
        return new AiStatusResponse(aiJobId, "completed", 100, "completed", null, null, null, null, null, null);
    }
    private AiResultResponse createMockResultResponse(String aiJobId) {
        return new AiResultResponse();
    }
}