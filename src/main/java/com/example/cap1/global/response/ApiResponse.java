package com.example.cap1.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class ApiResponse {

    private final Boolean success;
    private final Integer code;
    private final String message;

    public static ApiResponse of(Boolean success, Code code) {
        return new ApiResponse(success, code.getCode(), code.getMessage());
    }

    public static ApiResponse of(Boolean success, Code errorCode, Exception e) {
        return new ApiResponse(success, errorCode.getCode(), errorCode.getMessage(e));
    }

    public static ApiResponse of(Boolean success, Code errorCode, String message) {
        return new ApiResponse(success, errorCode.getCode(), errorCode.getMessage(message));
    }
}
