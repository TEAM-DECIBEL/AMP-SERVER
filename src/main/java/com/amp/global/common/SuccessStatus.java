package com.amp.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements SuccessCode {
    // 공통 성공 코드
    OK(HttpStatus.OK, "COM_200", "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "COM_201", "리소스가 성공적으로 생성되었습니다."),

    // 페스티벌 관련 성공 코드
    FESTIVAL_CREATE_SUCCESS(HttpStatus.CREATED, "FES_201", "공연 등록이 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String msg;
}
