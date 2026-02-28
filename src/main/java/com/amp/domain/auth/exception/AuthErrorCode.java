package com.amp.domain.auth.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    // 401 Unauthorized - 인증 실패
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH", "001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH", "002", "만료된 토큰입니다."),
    TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH", "003", "인증 토큰이 없습니다."),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH", "004", "토큰 형식이 올바르지 않습니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH", "005", "지원하지 않는 토큰 형식입니다."),
    TOKEN_SIGNATURE_INVALID(HttpStatus.UNAUTHORIZED, "AUTH", "006", "토큰 서명이 유효하지 않습니다."),

    // 401 Unauthorized - OAuth2 실패
    OAUTH2_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH", "010", "소셜 로그인에 실패했습니다."),
    OAUTH2_EMAIL_NOT_FOUND(HttpStatus.UNAUTHORIZED, "AUTH", "011", "소셜 계정에서 이메일 정보를 가져올 수 없습니다."),
    OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.UNAUTHORIZED, "AUTH", "012", "지원하지 않는 소셜 로그인 제공자입니다."),

    // 403 Forbidden - 접근 제한
    DOMAIN_ROLE_MISMATCH(HttpStatus.FORBIDDEN, "AUTH", "001", "접근 도메인과 사용자 역할이 일치하지 않습니다."),
    ONBOARDING_REQUIRED(HttpStatus.FORBIDDEN, "AUTH", "002", "온보딩을 완료해주세요."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH", "003", "접근 권한이 없습니다."),
    AUDIENCE_ONLY(HttpStatus.FORBIDDEN, "AUTH", "004", "관객 전용 기능입니다."),
    ORGANIZER_ONLY(HttpStatus.FORBIDDEN, "AUTH", "005", "주최자 전용 기능입니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
