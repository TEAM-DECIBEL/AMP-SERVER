package com.amp.domain.organizer.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrganizerErrorCode implements ErrorCode {
    // 404 Not Found
    ORGANIZER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORG", "001", "주최사 정보를 찾을 수 없습니다."),

    // 403 Forbidden
    ORGANIZER_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "ORG", "001", "주최사 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }

}
