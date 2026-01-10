package com.amp.global.response.error;


import com.amp.global.common.ErrorCode;

import java.time.LocalDateTime;

public record BaseErrorResponse(String code, String msg,  LocalDateTime timestamp) {
    public static BaseErrorResponse of(ErrorCode errorCode) {
        return new BaseErrorResponse(
                errorCode.getCode(),
                errorCode.getMsg(),
                LocalDateTime.now()
                );
    }
}