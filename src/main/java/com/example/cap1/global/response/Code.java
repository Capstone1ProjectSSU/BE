package com.example.cap1.global.response;

import com.example.cap1.global.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

@Getter
@RequiredArgsConstructor
public enum Code {

    OK(0, HttpStatus.OK, "Ok"),
    ENTITY_NOT_FOUND(404, HttpStatus.NOT_FOUND, " Entity Not Found"),

    BAD_REQUEST(1001, HttpStatus.BAD_REQUEST, "Bad request"),
    VALIDATION_ERROR(1002, HttpStatus.BAD_REQUEST, "Validation error"),
    INTERNAL_ERROR(1003, HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"),

    // User
    USER_NOT_FOUND(4001, HttpStatus.NOT_FOUND, " User Not Found"),
    USER_SAME(4002, HttpStatus.BAD_REQUEST, "이미 존재하는 유저입니다."),

    // Audio
    AUDIO_NOT_FOUND(5001, HttpStatus.NOT_FOUND, "음원을 찾을 수 없습니다."),
    AUDIO_FILE_EMPTY(5002, HttpStatus.BAD_REQUEST, "음원 파일이 비어있습니다."),
    AUDIO_FILE_TOO_LARGE(5003, HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기는 50MB를 초과할 수 없습니다."),
    AUDIO_INVALID_FORMAT(5004, HttpStatus.BAD_REQUEST, "MP3 형식만 지원됩니다."),
    AUDIO_FORBIDDEN(5005, HttpStatus.FORBIDDEN, "음원에 대한 접근 권한이 없습니다."),

    // Sheet
    SHEET_NOT_FOUND(6001, HttpStatus.NOT_FOUND, "악보를 찾을 수 없습니다."),
    SHEET_FORBIDDEN(6002, HttpStatus.FORBIDDEN, "악보에 대한 접근 권한이 없습니다."),
    ;


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
    public String getMessage(Throwable e) {
        return this.getMessage(this.getMessage() + " - " + e.getMessage());
        // 결과 예시 - "Validation error - Reason why it isn't valid"
    }

    public String getMessage(String message) {
        return Optional.ofNullable(message)
                .filter(Predicate.not(String::isBlank))
                .orElse(this.getMessage());
    }

    public static Code valueOf(HttpStatus httpStatus) {
        if (httpStatus == null) {
            throw new GeneralException("HttpStatus is null.");
        }
        return Arrays.stream(values())
                .filter(errorCode -> errorCode.getHttpStatus() == httpStatus)
                .findFirst()
                .orElseGet(() -> {
                    if (httpStatus.is4xxClientError()) {
                        return Code.BAD_REQUEST;
                    } else if (httpStatus.is5xxServerError()) {
                        return Code.INTERNAL_ERROR;
                    } else {
                        return Code.OK;
                    }
                });
    }


    @Override
    public String toString() {
        return String.format("%s (%d)", this.name(), this.getCode());
    }
}
