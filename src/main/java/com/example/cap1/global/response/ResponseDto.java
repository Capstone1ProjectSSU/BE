package com.example.cap1.global.response;

import lombok.Getter;

@Getter
public class ResponseDto<T> extends ApiResponse {

    private final T data;

    private ResponseDto(T data) {
        super(true, Code.OK.getCode(), Code.OK.getMessage());
        this.data = data;
    }

    private ResponseDto(T data, String message) {
        super(true, Code.OK.getCode(), message);
        this.data = data;
    }

    public static <T> ResponseDto<T> of(T data) {
        return new ResponseDto<>(data);
    }

    public static <T> ResponseDto<T> of(T data, String message) {
        return new ResponseDto<>(data, message);
    }

    public static <T> ResponseDto<T> empty() {
        return new ResponseDto<>(null);
    }
}
