package com.amp.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COM", "001", "잘못된 입력값입니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "COM", "002", "입력값의 타입이 일치하지 않습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "COM", "003", "필수 파라미터가 누락되었습니다."),
    INVALID_JSON(HttpStatus.BAD_REQUEST, "COM", "004", "JSON 파싱 중 오류가 발생했습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COM", "001", "로그인이 필요한 서비스입니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "COM", "001", "해당 요청에 대한 권한이 없습니다."),

    // 404 Not Found
    NOT_FOUND(HttpStatus.NOT_FOUND, "COM", "001", "찾을 수 없는 리소스입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM", "001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
