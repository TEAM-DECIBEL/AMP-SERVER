package com.amp.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessStatus implements SuccessCode {
    // COMMON
    OK(HttpStatus.OK, "COM", "001", "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "COM", "002", "리소스가 성공적으로 생성되었습니다."),

    // FESTIVAL
    FESTIVAL_CREATE_SUCCESS(HttpStatus.CREATED, "FES", "001", "공연 등록이 완료되었습니다."),
    GET_FESTIVAL_DETAIL_INFO(HttpStatus.OK, "FES", "002", "공연 상세 정보가 조회되었습니다."),
    FESTIVAL_UPDATE_SUCCESS(HttpStatus.OK, "FES", "003", "공연 정보 수정이 완료되었습니다."),
    FESTIVAL_DELETE_SUCCESS(HttpStatus.OK, "FES", "004", "공연 삭제가 완료되었습니다."),

    // NOTICE
    NOTICE_DETAIL_GET_SUCCESS(HttpStatus.OK, "NOT", "001", "공지 상세 조회가 완료되었습니다."),
    NOTICE_DELETE_SUCCESS(HttpStatus.OK, "NOT", "002", "공지가 삭제되었습니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }

}
