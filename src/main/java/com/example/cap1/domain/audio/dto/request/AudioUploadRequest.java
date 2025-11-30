package com.example.cap1.domain.audio.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class AudioUploadRequest {

    private MultipartFile audioFile;
    private String songTitle;
    private String artistName;

    public void validate() {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("오디오 파일은 필수입니다.");
        }

        if (songTitle == null || songTitle.trim().isEmpty()) {
            throw new IllegalArgumentException("곡 제목은 필수입니다.");
        }

        if (artistName == null || artistName.trim().isEmpty()) {
            throw new IllegalArgumentException("아티스트 이름은 필수입니다.");
        }
    }
}