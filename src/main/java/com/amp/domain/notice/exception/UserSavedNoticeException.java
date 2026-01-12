package com.amp.domain.notice.exception;

import com.amp.global.exception.CustomException;

public class UserSavedNoticeException extends CustomException {

    public UserSavedNoticeException(UserSavedNoticeErrorCode errorCode) {
        super(errorCode);
    }
}
