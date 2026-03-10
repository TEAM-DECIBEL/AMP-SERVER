package com.amp.domain.auth.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RegistrationErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_REGISTRATION_CODE(HttpStatus.BAD_REQUEST, "REG", "001", "가입코드가 올바르지 않습니다."),

    // 403 Forbidden
    EMAIL_NOT_REGISTERED(HttpStatus.FORBIDDEN, "REG", "002", "등록되지 않은 이메일입니다."),
    MAX_ATTEMPTS_EXCEEDED(HttpStatus.FORBIDDEN, "REG", "003", "최대 시도 횟수를 초과했습니다."),
    CODE_VERIFICATION_REQUIRED(HttpStatus.FORBIDDEN, "REG", "004", "가입코드 검증이 필요합니다."),

    // 409 Conflict
    ALREADY_VERIFIED(HttpStatus.CONFLICT, "REG", "005", "이미 검증이 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
