package com.amp.domain.notice.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements ErrorCode {
    // 404 Not Found
    INVALID_NOTICE(HttpStatus.NOT_FOUND, "NTC", "001", "잘못된 공지 값입니다."),

    // 500 Internal Server Error
    DELETE_NOTICE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "NTC", "001", "공지를 삭제하지 못했습니다.");


    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
