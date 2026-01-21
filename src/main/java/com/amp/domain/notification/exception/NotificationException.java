package com.amp.domain.notification.exception;

import com.amp.global.exception.CustomException;

public class NotificationException extends CustomException {

    public NotificationException(NotificationErrorCode notificationErrorCode) {
        super(notificationErrorCode);
    }
}
