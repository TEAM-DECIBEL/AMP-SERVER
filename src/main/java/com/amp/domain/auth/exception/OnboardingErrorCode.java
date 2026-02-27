package com.amp.domain.auth.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OnboardingErrorCode implements ErrorCode {
    // 400 Bad Request - 필수 값 누락
    ALREADY_COMPLETED_ONBOARDING(HttpStatus.BAD_REQUEST, "OBD", "001", "이미 온보딩이 완료되었습니다."),
    INVALID_USER_TYPE(HttpStatus.BAD_REQUEST, "OBD", "002", "유효하지 않은 사용자 타입입니다."),
    ORGANIZER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "OBD", "003", "주최사명은 필수입니다."),
    NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST, "OBD", "004", "닉네임은 필수입니다."),
    USER_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "OBD", "005", "사용자 타입은 필수입니다."),
    USER_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "OBD", "006", "요청한 사용자 타입이 현재 설정된 타입과 일치하지 않습니다."),

    // 400 Bad Request - 유효성 검증 실패
    NICKNAME_LENGTH_INVALID(HttpStatus.BAD_REQUEST, "OBD", "010", "닉네임은 2-12자 사이여야 합니다."),
    ORGANIZER_NAME_LENGTH_INVALID(HttpStatus.BAD_REQUEST, "OBD", "011", "주최사명은 2-12자 사이여야 합니다."),
    NICKNAME_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "OBD", "012", "닉네임 형식이 올바르지 않습니다."),
    ORGANIZER_NAME_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "OBD", "013", "주최사명 형식이 올바르지 않습니다."),

    // 409 Conflict - 중복
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "OBD", "001", "이미 사용 중인 닉네임입니다."),
    DUPLICATE_ORGANIZER_NAME(HttpStatus.CONFLICT, "OBD", "002", "이미 사용 중인 주최사명입니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
