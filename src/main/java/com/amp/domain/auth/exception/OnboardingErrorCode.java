package com.amp.domain.auth.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OnboardingErrorCode implements ErrorCode  {
    // 400 Bad Request
    ALREADY_COMPLETED_ONBOARDING(HttpStatus.BAD_REQUEST,"OBD","001", "이미 온보딩이 완료되었습니다."),
    INVALID_USER_TYPE(HttpStatus.BAD_REQUEST,"OBD", "002","유효하지 않은 사용자 타입입니다."),
    ORGANIZER_NAME_REQUIRED(HttpStatus.BAD_REQUEST,"OBD", "003","주최사명은 필수입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST,"OBD", "004","이미 사용 중인 닉네임입니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
