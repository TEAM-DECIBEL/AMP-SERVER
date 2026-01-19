package com.amp.domain.stage.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StageErrorCode implements ErrorCode {

    // 400 BAD REQUEST
    FESTIVAL_ENDED(HttpStatus.BAD_REQUEST, "STA", "001", "종료된 공연입니다."),
    ALREADY_REPORTED_RECENTLY(HttpStatus.BAD_REQUEST, "STA", "002", "15분 후에 다시 입력할 수 있습니다."),
    TOO_EARLY_TO_REPORT(HttpStatus.BAD_REQUEST, "STA", "003", "공연 시작 8시간 전부터 입력 가능합니다."),
    DAILY_INPUT_CLOSED(HttpStatus.BAD_REQUEST, "STA", "004", "입력 가능한 시간이 지났습니다."),
    NO_SCHEDULE_TODAY(HttpStatus.BAD_REQUEST, "STA", "005", "오늘은 등록된 무대/부스 일정이 없습니다."),
    INVALID_CONGESTION_LEVEL(HttpStatus.BAD_REQUEST, "STA", "006", "CROWDED / NORMAL / SMOOTH 값 중에만 입력 가능합니다."),

    // 404 NOT FOUND
    STAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "STA", "001", "무대를 찾을 수 없습니다."),
    CONGESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "STA", "002", "혼잡도 정보를 찾을 수 없습니다."),
    NO_SCHEDULE_FOUND(HttpStatus.NOT_FOUND, "STA", "003", "등록된 무대 정보를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
