package com.example.cap1.domain.audio.service;

import com.example.cap1.domain.audio.domain.Audio;
import com.example.cap1.domain.audio.dto.request.AudioUploadRequest;
import com.example.cap1.domain.audio.dto.response.AudioUploadResponse;
import com.example.cap1.domain.audio.repository.AudioRepository;
import com.example.cap1.global.exception.GeneralException;
import com.example.cap1.global.response.Code;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudioService {

    private final AudioRepository audioRepository;
    private final FileStorageService fileStorageService;

    /**
     * 음원 파일을 업로드하고 메타데이터를 저장합니다.
     *
     * @param userId 사용자 ID (JWT에서 추출 예정)
     * @param request 업로드 요청 정보
     * @return 업로드된 음원 정보
     */
    @Transactional
    public AudioUploadResponse uploadAudio(Long userId, AudioUploadRequest request) {
        log.info("음원 업로드 시작 - userId: {}, title: {}", userId, request.getSongTitle());

        // 요청 데이터 검증
        request.validate();

        MultipartFile audioFile = request.getAudioFile();

        // 파일 저장
        String filePath = fileStorageService.storeFile(audioFile);

        // Audio 엔티티 생성 및 저장
        Audio audio = Audio.create(
                userId,
                request.getSongTitle(),
                request.getArtistName(),
                filePath,
                audioFile.getSize()
        );

        Audio savedAudio = audioRepository.save(audio);

        log.info("음원 업로드 완료 - audioId: {}", savedAudio.getId());

        return AudioUploadResponse.from(savedAudio);
    }

    /**
     * 음원 ID로 음원 정보를 조회합니다.
     *
     * @param audioId 음원 ID
     * @return 음원 엔티티
     */
    public Audio getAudioById(Long audioId) {
        return audioRepository.findById(audioId)
                .orElseThrow(() -> new GeneralException(Code.AUDIO_NOT_FOUND));
    }

    /**
     * 음원을 삭제합니다.
     *
     * @param audioId 음원 ID
     * @param userId 사용자 ID
     */
    @Transactional
    public void deleteAudio(Long audioId, Long userId) {
        Audio audio = getAudioById(audioId);

        // 권한 확인
        if (!audio.getUserId().equals(userId)) {
            throw new GeneralException(Code.AUDIO_FORBIDDEN);
        }

        // 파일 삭제
        fileStorageService.deleteFile(audio.getFilePath());

        // DB에서 삭제
        audioRepository.delete(audio);

        log.info("음원 삭제 완료 - audioId: {}", audioId);
    }
}