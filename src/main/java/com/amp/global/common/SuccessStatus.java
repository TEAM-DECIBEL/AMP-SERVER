package com.amp.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements SuccessCode {
    // COMMON
    OK(HttpStatus.OK, "COM","001", "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "COM" ,"002", "리소스가 성공적으로 생성되었습니다."),

    // FESTIVAL
    FESTIVAL_CREATE_SUCCESS(HttpStatus.CREATED, "FES","001", "공연 등록이 완료되었습니다."),

    // UserFestival
    USER_FESTIVAL_RECENT_FOUND(HttpStatus.OK, "UFE", "001", "관람 예정 최근에 보는 공연 정보가 조회되었습니다."),
    USER_FESTIVAL_RECENT_NOT_FOUND(HttpStatus.OK,"UFE","002","관람 예정 정보가 없습니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }

}
