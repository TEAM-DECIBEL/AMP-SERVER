package com.amp.domain.auth.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum RegistrationErrorCode implements ErrorCode {

    // 400 Bad Request - 잘못된 입력값
    INVALID_REGISTRATION_CODE(HttpStatus.BAD_REQUEST, "REG", "001", "가입코드가 올바르지 않습니다."),

    // 400 Bad Request - 등록되지 않은 이메일
    EMAIL_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "REG", "002", "등록되지 않은 이메일입니다."),

    // 403 Forbidden - 검증 미완료로 접근 권한 없음
    CODE_VERIFICATION_REQUIRED(HttpStatus.FORBIDDEN, "REG", "004", "가입코드 검증이 필요합니다."),

    // 409 Conflict - 이미 완료된 상태와 충돌
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
