package com.amp.global.response.success;

import org.springframework.http.HttpStatus;

public record BaseResponse<T>(int code, String msg, T data) {

    public static <T> BaseResponse<T> ok(String msg, T data) {
        return new BaseResponse<>(HttpStatus.OK.value(), msg, data);
    }

    public static <T> BaseResponse<T> create(String msg, T data) {
        return new BaseResponse<>(HttpStatus.CREATED.value(), msg, data);
    }

    public static <T> BaseResponse<T> noContent(String msg) {
        return new BaseResponse<>(HttpStatus.NO_CONTENT.value(), msg, null);
    }
}
