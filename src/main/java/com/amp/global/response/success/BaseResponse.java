package com.amp.global.response.success;

import com.amp.global.common.SuccessStatus;
import lombok.Builder;
import org.springframework.http.HttpStatus;

@Builder
public record BaseResponse<T>(int status, String msg, T data) {

    public static <T> BaseResponse<T> ok(String msg, T data) {
        return new BaseResponse<>(HttpStatus.OK.value(), msg, data);
    }

    public static <T> BaseResponse<T> create(String msg, T data) {
        return new BaseResponse<>(HttpStatus.CREATED.value(), msg, data);
    }

    public static <T> BaseResponse<T> noContent(String msg) {
        return new BaseResponse<>(HttpStatus.NO_CONTENT.value(), msg, null);
    }

    public static <T> BaseResponse<T> of(SuccessStatus successStatus, T data) {
        return BaseResponse.<T>builder()
                .status(successStatus.getHttpStatus().value())
                .msg(successStatus.getMsg())
                .data(data)
                .build();
    }
}
