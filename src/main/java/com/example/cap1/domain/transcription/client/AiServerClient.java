package com.example.cap1.domain.transcription.client;

import com.example.cap1.domain.transcription.dto.ai.AiEnqueueResponse;
import com.example.cap1.domain.transcription.dto.ai.AiResultResponse;
import com.example.cap1.domain.transcription.dto.ai.AiStatusResponse;
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Value("${file.transcription-dir:./uploads/transcription}")
    private String transcriptionDir;

    @Value("${ai.server.mock-mode:true}")
    private boolean mockMode;

    private final RestTemplate restTemplate;

    /**
     * E2E Task 등록 (음원 분리 + MIDI 변환 + 코드 인지)
     */
    public AiEnqueueResponse enqueueE2ETask(String audioFilePath, String instrument) {
        if (mockMode) {
            log.warn("⚠️ AI 서버 Mock 모드 활성화 - 실제 AI 서버에 요청하지 않음");
            return createMockEnqueueResponse();
        }

        return callRealAiServer(audioFilePath, instrument);
    }

    /**
     * 작업 상태 조회
     */
    public AiStatusResponse getTaskStatus(String aiJobId) {
        if (mockMode) {
            log.warn("⚠️ AI 서버 Mock 모드 - 가짜 상태 반환");
            return createMockStatusResponse(aiJobId);
        }

        String url = aiServerBaseUrl + "/tasks/e2e-base/status/" + aiJobId;

        try {
            log.info("AI 서버 상태 조회 - URL: {}, aiJobId: {}", url, aiJobId);

            ResponseEntity<AiStatusResponse> response = restTemplate.getForEntity(
                    url,
                    AiStatusResponse.class
            );

            AiStatusResponse result = response.getBody();

            log.info("AI 서버 상태 조회 성공 - aiJobId: {}, status: {}, progress: {}%",
                    aiJobId, result.getStatus(), result.getProgressPercent());

            return result;

        } catch (RestClientException e) {
            log.error("AI 서버 상태 조회 실패 - aiJobId: {}", aiJobId, e);
            throw new GeneralException(Code.AI_SERVER_ERROR,
                    "AI 서버 상태 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 작업 결과 조회
     */
    public AiResultResponse getTaskResult(String aiJobId) {
        if (mockMode) {
            log.warn("⚠️ AI 서버 Mock 모드 - 가짜 결과 반환");
            return createMockResultResponse(aiJobId);
        }

        String url = aiServerBaseUrl + "/tasks/e2e-base/result/" + aiJobId;

        try {
            log.info("AI 서버 결과 조회 - URL: {}, aiJobId: {}", url, aiJobId);

            ResponseEntity<AiResultResponse> response = restTemplate.getForEntity(
                    url,
                    AiResultResponse.class
            );

            AiResultResponse result = response.getBody();

            log.info("AI 서버 결과 조회 성공 - aiJobId: {}", aiJobId);

            return result;

        } catch (RestClientException e) {
            log.error("AI 서버 결과 조회 실패 - aiJobId: {}", aiJobId, e);
            throw new GeneralException(Code.AI_SERVER_ERROR,
                    "AI 서버 결과 조회 실패: " + e.getMessage());
        }
    }

    // ========== 🆕 파일 다운로드 메서드들 ==========

    /**
     * 모든 결과 파일을 다운로드
     */
    public void downloadAllFiles(String aiJobId, AiResultResponse result) {
        try {
            // 저장 디렉토리 생성
            Path baseDir = Paths.get(transcriptionDir).resolve(aiJobId);
            Files.createDirectories(baseDir);

            log.info("결과 파일 다운로드 시작 - aiJobId: {}, baseDir: {}", aiJobId, baseDir);

            // 1. 음원 분리 파일들
            downloadSeparatedTracks(aiJobId, result.getOutputs().getSeparatedTracks(), baseDir);

            // 2. MIDI 파일
            downloadMidi(aiJobId, result.getOutputs().getTranscriptionUrl(), baseDir);

            // 3. 코드 진행 파일들
            downloadChordProgression(aiJobId, result.getOutputs().getChordProgression(), baseDir);

            log.info("✅ 모든 결과 파일 다운로드 완료 - aiJobId: {}", aiJobId);

        } catch (IOException e) {
            log.error("파일 다운로드 실패 - aiJobId: {}", aiJobId, e);
            throw new GeneralException(Code.INTERNAL_ERROR, "파일 다운로드 실패: " + e.getMessage());
        }
    }

    /**
     * 음원 분리 파일들 다운로드
     */
    private void downloadSeparatedTracks(String aiJobId,
                                         AiResultResponse.SeparatedTracks tracks,
                                         Path baseDir) throws IOException {
        if (tracks == null) {
            log.warn("음원 분리 결과 없음 - 스킵");
            return;
        }

        Path separatedDir = baseDir.resolve("separated");
        Files.createDirectories(separatedDir);

        // Guitar
        if (tracks.getGuitarTrackUrl() != null) {
            downloadFile(tracks.getGuitarTrackUrl(),
                    separatedDir.resolve("guitar.opus"));
        }

        // Bass
        if (tracks.getBassTrackUrl() != null) {
            downloadFile(tracks.getBassTrackUrl(),
                    separatedDir.resolve("bass.opus"));
        }

        // Vocal
        if (tracks.getVocalTrackUrl() != null) {
            downloadFile(tracks.getVocalTrackUrl(),
                    separatedDir.resolve("vocal.opus"));
        }

        // Drums
        if (tracks.getDrumsTrackUrl() != null) {
            downloadFile(tracks.getDrumsTrackUrl(),
                    separatedDir.resolve("drums.opus"));
        }

        log.info("✅ 음원 분리 파일 다운로드 완료");
    }

    /**
     * MIDI 파일 다운로드
     */
    private void downloadMidi(String aiJobId, String midiUrl, Path baseDir) throws IOException {
        if (midiUrl == null) {
            log.warn("MIDI 파일 URL 없음 - 스킵");
            return;
        }

        Path midiPath = baseDir.resolve("transcription.mid");
        downloadFile(midiUrl, midiPath);

        log.info("✅ MIDI 파일 다운로드 완료");
    }

    /**
     * 코드 진행 파일들 다운로드
     */
    private void downloadChordProgression(String aiJobId,
                                          AiResultResponse.ChordProgression chords,
                                          Path baseDir) throws IOException {
        if (chords == null) {
            log.warn("코드 진행 결과 없음 - 스킵");
            return;
        }

        // JSON 파일
        if (chords.getJsonUrl() != null) {
            downloadFile(chords.getJsonUrl(),
                    baseDir.resolve("chord_progression.json"));
        }

        // TXT 파일
        if (chords.getTxtUrl() != null) {
            downloadFile(chords.getTxtUrl(),
                    baseDir.resolve("chord_progression.txt"));
        }

        log.info("✅ 코드 진행 파일 다운로드 완료");
    }

    /**
     * 단일 파일 다운로드 (공통 메서드)
     */
    private void downloadFile(String url, Path localPath) throws IOException {
        if (mockMode) {
            log.debug("Mock 모드 - 파일 다운로드 스킵: {}", localPath.getFileName());
            return;
        }

        try {
            log.debug("파일 다운로드 시작 - URL: {}, localPath: {}", url, localPath);

            // AI 서버에서 파일 다운로드
            ResponseEntity<byte[]> response = restTemplate.getForEntity(
                    aiServerBaseUrl + url,
                    byte[].class
            );

            byte[] fileBytes = response.getBody();
            if (fileBytes == null || fileBytes.length == 0) {
                throw new IOException("다운로드된 파일이 비어있습니다: " + url);
            }

            // 로컬 파일로 저장
            Files.write(localPath, fileBytes);

            log.debug("✅ 파일 다운로드 완료 - size: {} bytes, path: {}",
                    fileBytes.length, localPath);

        } catch (RestClientException e) {
            log.error("파일 다운로드 실패 - URL: {}", url, e);
            throw new IOException("파일 다운로드 실패: " + e.getMessage(), e);
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * 실제 AI 서버 호출
     */
    private AiEnqueueResponse callRealAiServer(String audioFilePath, String instrument) {
        String url = aiServerBaseUrl + "/tasks/e2e-base/enqueue";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

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

            log.info("AI 서버 E2E Task 등록 - URL: {}, instrument: {}", url, instrument);

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
                    "AI 서버 통신 실패: " + e.getMessage());
        }
    }

    // ========== Mock Response Generators ==========

    /**
     * Mock Enqueue 응답 생성
     */
    private AiEnqueueResponse createMockEnqueueResponse() {
        String mockJobId = "mock-ai-job-" + System.currentTimeMillis();
        String queuedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        log.info("✅ Mock AI Job 생성 - jobId: {}", mockJobId);

        return new AiEnqueueResponse(mockJobId, "queued", queuedAt);
    }

    /**
     * Mock Status 응답 생성
     */
    private AiStatusResponse createMockStatusResponse(String aiJobId) {
        // Mock: 항상 완료 상태 반환하되, 단계별 정보 포함

        AiStatusResponse.AvailableArtifacts artifacts =
                new AiStatusResponse.AvailableArtifacts(
                        true,   // separatedTracksReady
                        true,   // transcriptionReady
                        true    // chordProgressionReady
                );

        return new AiStatusResponse(
                aiJobId,
                "completed",  // status
                100,          // progressPercent
                "completed",  // currentStage
                artifacts,    // availableArtifacts
                LocalDateTime.now().minusMinutes(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.now().minusMinutes(4).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                null          // error
        );
    }

    /**
     * Mock Result 응답 생성
     */
    private AiResultResponse createMockResultResponse(String aiJobId) {
        // Mock 데이터 생성
        AiResultResponse.SeparatedTracks tracks = new AiResultResponse.SeparatedTracks(
                "/files/separated/guitar_track.opus",
                "/files/separated/bass_track.opus",
                "/files/separated/vocal_track.opus",
                "/files/separated/drums_track.opus"
        );

        AiResultResponse.ChordProgression chords = new AiResultResponse.ChordProgression(
                "/files/chords/progression.json",
                "/files/chords/progression.txt"
        );

        AiResultResponse.Metadata metadata = new AiResultResponse.Metadata(
                120,  // tempo
                "C",  // key
                243L, // duration
                "4/4" // time signature
        );

        AiResultResponse.Outputs outputs = new AiResultResponse.Outputs(
                tracks,
                "/files/midi/transcription.mid",
                chords,
                metadata
        );

        log.info("✅ Mock AI Result 생성 - aiJobId: {}", aiJobId);

        return new AiResultResponse(aiJobId, outputs);
    }
}