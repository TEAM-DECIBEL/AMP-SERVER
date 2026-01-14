package com.amp.domain.notice.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements ErrorCode {
    // 400 Bad Request
    NOTICE_ALREADY_DELETED(HttpStatus.BAD_REQUEST, "NTC", "001", "이미 삭제된 공지입니다."),

    // 403 Forbidden
    NOTICE_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "NTC", "001", "작성자 유저만 공지글을 삭제할 수 있습니다."),

    // 404 Not Found
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NTC", "001", "존재하지 않는 공지 아이디입니다."),

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
