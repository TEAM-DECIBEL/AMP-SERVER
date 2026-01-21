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
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COM", "001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "COM", "002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "COM", "003", "만료된 토큰입니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "COM", "004", "사용자를 찾을 수 없습니다."),
    OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "COM", "005", "소셜 로그인에 실패했습니다."),
    EMAIL_NOT_FOUND(HttpStatus.UNAUTHORIZED, "COM", "006", "이메일 정보를 가져올 수 없습니다."),
    PROVIDER_ID_NOT_FOUND(HttpStatus.UNAUTHORIZED, "COM", "007", "인증 제공자 정보를 가져올 수 없습니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "COM", "001", "접근 권한이 없습니다."),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "COM", "002", "권한이 부족합니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COM", "001", "지원하지 않는 HTTP 메소드입니다."),

    // 413 Payload Too Large
    EXCEED_MAXIMUM_SIZE(HttpStatus.PAYLOAD_TOO_LARGE, "COM", "001", "업로드 가능한 이미지 크기 (10MB)를 초과했습니다."),

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
