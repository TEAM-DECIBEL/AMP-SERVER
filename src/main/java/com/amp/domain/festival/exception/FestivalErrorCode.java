package com.amp.domain.festival.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FestivalErrorCode implements ErrorCode {

    // 400 Bad Request
    INVALID_FESTIVAL_PERIOD(HttpStatus.BAD_REQUEST, "FES", "001", "공연 종료일은 시작일보다 빠를 수 없습니다."),
    FESTIVAL_CREATE_FAILED(HttpStatus.BAD_REQUEST, "FES", "002", "페스티벌 생성에 실패하였습니다."),
    SCHEDULES_REQUIRED(HttpStatus.BAD_REQUEST, "FES", "003", "하나 이상의 공연 일자 입력은 필수입니다."),
    INVALID_SCHEDULE_FORMAT(HttpStatus.BAD_REQUEST, "FES", "004", "공연 일자 형식이 잘못되었습니다."),
    INVALID_STAGE_FORMAT(HttpStatus.BAD_REQUEST, "FES", "005", "무대/부스 형식이 잘못되었습니다."),
    INVALID_CATEGORY_FORMAT(HttpStatus.BAD_REQUEST, "FES", "006", "카테고리 형식이 잘못되었습니다."),

    // 404 Not Found
    FESTIVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "FES", "001", "존재하지 않는 공연 정보입니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
