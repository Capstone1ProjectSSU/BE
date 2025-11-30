package com.example.cap1.domain.audio.service;

import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String ALLOWED_EXTENSION = ".mp3";

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path uploadPath;

    /**
     * 서비스 초기화 시 업로드 디렉토리를 생성합니다.
     */
    @PostConstruct
    public void init() {
        try {
            this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.uploadPath);
            log.info("파일 업로드 디렉토리 생성 완료: {}", this.uploadPath);
        } catch (IOException e) {
            log.error("파일 업로드 디렉토리 생성 실패: {}", uploadDir, e);
            throw new RuntimeException("파일 업로드 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    /**
     * 파일을 로컬 시스템에 저장합니다.
     *
     * @param file 업로드할 파일
     * @return 저장된 파일의 경로
     */
    public String storeFile(MultipartFile file) {
        validateFile(file);

        try {
            // UUID로 파일명 생성
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 파일 저장
            Path targetLocation = this.uploadPath.resolve(newFilename);
            file.transferTo(targetLocation.toFile());

            log.info("파일 저장 완료: {}", targetLocation);

            return "/uploads/audio/" + newFilename;

        } catch (IOException e) {
            log.error("파일 저장 실패", e);
            throw new GeneralException(Code.INTERNAL_ERROR, "파일 저장에 실패했습니다.");
        }
    }

    /**
     * 파일을 삭제합니다.
     *
     * @param filePath 삭제할 파일의 경로
     */
    public void deleteFile(String filePath) {
        try {
            String filename = filePath.replace("/uploads/audio/", "");
            Path path = this.uploadPath.resolve(filename);
            Files.deleteIfExists(path);
            log.info("파일 삭제 완료: {}", path);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filePath, e);
        }
    }

    /**
     * 파일 유효성을 검증합니다.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new GeneralException(Code.AUDIO_FILE_EMPTY);
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new GeneralException(Code.AUDIO_FILE_TOO_LARGE);
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(ALLOWED_EXTENSION)) {
            throw new GeneralException(Code.AUDIO_INVALID_FORMAT);
        }
    }

    /**
     * 파일 확장자를 추출합니다.
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return ALLOWED_EXTENSION;
        }
        int lastDotIndex = filename.lastIndexOf(".");
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : ALLOWED_EXTENSION;
    }
}