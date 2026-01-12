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
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"401_001", "인증이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED,"401_002", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,"401_003", "만료된 토큰입니다."),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED,"401_004", "사용자를 찾을 수 없습니다."),
    OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED,"401_005", "소셜 로그인에 실패했습니다." ),
    EMAIL_NOT_FOUND(HttpStatus.UNAUTHORIZED,"401_006", "이메일 정보를 가져올 수 없습니다."),
    PROVIDER_ID_NOT_FOUND(HttpStatus.UNAUTHORIZED,"401_007", "인증 제공자 정보를 가져올 수 없습니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN,"403_001", "접근 권한이 없습니다."),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN,"403_002","권한이 부족합니다."),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "405_001", "지원하지 않는 HTTP 메소드입니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500_001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
