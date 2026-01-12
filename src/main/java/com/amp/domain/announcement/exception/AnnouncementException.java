package com.amp.domain.announcement.exception;

import com.amp.global.common.ErrorCode;
import com.amp.global.exception.CustomException;

public class AnnouncementException extends CustomException {

    public AnnouncementException(ErrorCode errorCode) {
        super(errorCode);
    }
}
