package com.amp.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    // 400 Bad Request
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "400_001", "잘못된 입력값입니다."),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "400_002", "입력값의 타입이 일치하지 않습니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "400_003", "필수 파라미터가 누락되었습니다."),
    INVALID_JSON(HttpStatus.BAD_REQUEST, "400_004", "JSON 파싱 중 오류가 발생했습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "401_001", "로그인이 필요한 서비스입니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "403_001", "해당 요청에 대한 권한이 없습니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "405_001", "지원하지 않는 HTTP 메소드입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500_001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String msg;
}
