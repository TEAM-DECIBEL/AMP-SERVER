package com.amp.domain.notice.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements ErrorCode {
    // 404 Not Found
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NTC", "001", "존재하지 않는 공지 아이디입니다.");


    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
