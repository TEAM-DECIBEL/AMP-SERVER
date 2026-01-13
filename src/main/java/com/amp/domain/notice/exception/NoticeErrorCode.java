package com.amp.domain.notice.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements ErrorCode {
    // 400 Bad Request
    INVALID_NOTICE(HttpStatus.BAD_REQUEST, "NTC", "001", "잘못된 공지 값입니다.");

    // 401 Unauthorized

    // 403 Forbidden

    // 405 Method Not Allowed
    // 500 Internal Server Error

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
