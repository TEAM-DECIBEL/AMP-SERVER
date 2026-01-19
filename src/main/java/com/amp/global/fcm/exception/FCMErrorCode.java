package com.amp.global.fcm.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FCMErrorCode implements ErrorCode {

    // 400 Bad Request
    ALREADY_SUBSCRIBED(HttpStatus.BAD_REQUEST, "FCM", "001", "이미 구독 중인 카테고리입니다."),
    NOT_SUBSCRIBED_CATEGORY(HttpStatus.BAD_REQUEST, "FCM", "002", "구독하지 않은 카테고리입니다."),

    // 500 Internal Server Error
    FAIL_TO_SEND_PUSH_ALARM(HttpStatus.INTERNAL_SERVER_ERROR, "FCM", "001", "푸시알림 발송에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
