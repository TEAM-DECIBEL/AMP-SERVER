package com.amp.domain.festival.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FestivalErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_FESTIVAL_PERIOD(HttpStatus.BAD_REQUEST, "F", "001", "공연 종료일은 시작일보다 빠를 수 없습니다."),

    // 404 Not Found
    FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "F", "001", "존재하지 않는 공연 정보입니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering; //
    }
}
