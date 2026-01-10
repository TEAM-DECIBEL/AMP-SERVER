package com.amp.global.response.error;


import com.amp.global.common.ErrorCode;

public record BaseErrorResponse(int status, String code, String msg) {
    public static BaseErrorResponse of(ErrorCode errorCode) {
        return new BaseErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMsg()
        );
    }
}