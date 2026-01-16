package com.amp.domain.notice.common.exception;

import com.amp.global.common.ErrorCode;
import com.amp.global.exception.CustomException;

public class NoticeException extends CustomException {

    public NoticeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
