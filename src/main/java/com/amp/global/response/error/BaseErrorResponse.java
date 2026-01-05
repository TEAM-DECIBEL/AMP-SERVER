package com.amp.global.response.error;


import com.amp.global.common.ErrorCode;

public record BaseErrorResponse(String code, String msg) {
    public static BaseErrorResponse of(ErrorCode errorCode) {
        return new BaseErrorResponse(
                errorCode.getCode(),
                errorCode.getMsg()
        );
    }
}