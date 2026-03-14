package com.amp.global.response.error;

import com.amp.global.common.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthErrorResponse {
    private final int status;
    private final String code;
    private final String msg;
    private final Instant timestamp;

    // 도메인 불일치 시 추가 정보
    private final String correctDomain;
    private final String userType;

    // 온보딩 필요 시 추가 정보
    private final String onboardingUrl;

    // 가입코드 검증 필요 시 추가 정보
    private final String verificationUrl;

    public static AuthErrorResponse of(ErrorCode errorCode) {
        return AuthErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .msg(errorCode.getMsg())
                .timestamp(Instant.now())
                .build();
    }

    public static AuthErrorResponse of(ErrorCode errorCode, Map<String, Object> details) {
        AuthErrorResponseBuilder builder = AuthErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .msg(errorCode.getMsg())
                .timestamp(Instant.now());

        if (details != null) {
            if (details.containsKey("correctDomain")) {
                builder.correctDomain((String) details.get("correctDomain"));
            }
            if (details.containsKey("userType")) {
                builder.userType((String) details.get("userType"));
            }
            if (details.containsKey("onboardingUrl")) {
                builder.onboardingUrl((String) details.get("onboardingUrl"));
            }
        }

        return builder.build();
    }

    public static AuthErrorResponse domainRoleMismatch(ErrorCode errorCode, String correctDomain, String userType) {
        return AuthErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .msg(errorCode.getMsg())
                .timestamp(Instant.now())
                .correctDomain(correctDomain)
                .userType(userType)
                .build();
    }

    public static AuthErrorResponse onboardingRequired(ErrorCode errorCode, String onboardingUrl) {
        return AuthErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .msg(errorCode.getMsg())
                .timestamp(Instant.now())
                .onboardingUrl(onboardingUrl)
                .build();
    }

    public static AuthErrorResponse codeVerificationRequired(ErrorCode errorCode, String verificationUrl) {
        return AuthErrorResponse.builder()
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .msg(errorCode.getMsg())
                .timestamp(Instant.now())
                .verificationUrl(verificationUrl)
                .build();
    }
}
