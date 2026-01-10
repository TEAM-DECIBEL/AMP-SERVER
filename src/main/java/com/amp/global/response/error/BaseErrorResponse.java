package com.amp.global.response.error;


import com.amp.global.common.ErrorCode;

import java.time.Instant;

public record BaseErrorResponse(int status, String code, String msg,  Instant timestamp) {
    public static BaseErrorResponse of(ErrorCode errorCode) {
        return new BaseErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMsg(),
                Instant.now()
                );
    }
}