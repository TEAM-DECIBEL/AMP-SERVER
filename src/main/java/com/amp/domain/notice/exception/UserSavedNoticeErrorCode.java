package com.amp.domain.notice.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserSavedNoticeErrorCode implements ErrorCode {
    // 400 Bad Request

    // 401 Unauthorized

    // 403 Forbidden

    // 404 Not Found
    SAVED_NOTICE_NOT_EXIST(HttpStatus.NOT_FOUND, "SAV", "001", "해당 유저가 저장한 공지가 아닙니다");
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
