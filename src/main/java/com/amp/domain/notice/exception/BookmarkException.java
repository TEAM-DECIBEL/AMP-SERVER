package com.amp.domain.notice.exception;

import com.amp.global.exception.CustomException;

public class BookmarkException extends CustomException {

    public BookmarkException(BookmarkErrorCode errorCode) {
        super(errorCode);
    }
}
