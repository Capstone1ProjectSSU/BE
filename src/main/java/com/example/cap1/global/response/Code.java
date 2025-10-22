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
