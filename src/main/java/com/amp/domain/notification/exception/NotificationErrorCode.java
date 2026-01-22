package com.amp.domain.notification.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

    // Forbidden
    NOTIFICATION_FORBIDDEN(HttpStatus.FORBIDDEN, "NTF", "001", "알림 읽음처리가 완료되지 못했습니다."),

    // Not Found
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NTF", "001", "알림이 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
