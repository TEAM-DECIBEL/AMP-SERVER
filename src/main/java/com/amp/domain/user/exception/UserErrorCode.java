package com.amp.domain.user.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    // 401 UNAUTHORIZED
    USER_NOT_AUTHORIZED(HttpStatus.UNAUTHORIZED, "USE", "001", "해당 요청의 권한이 없습니다."),

    // 400 Bad Request
    USER_TYPE_UNCHANGEABLE(HttpStatus.BAD_REQUEST,"USE","001","권한을 바꿀 수 없습니다."),
    // 403 FORBIDDEN
    USER_NOT_AUTHENTICATED(HttpStatus.FORBIDDEN, "USE", "001", "유효하지 않은 권한입니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USE", "001", "존재하지 않는 유저 정보입니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
