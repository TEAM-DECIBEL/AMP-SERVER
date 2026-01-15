package com.amp.domain.category.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FestivalCategoryErrorCode implements ErrorCode {

    // 404 NOT FOUND
    NOTICE_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CAT", "001", "존재하지 않는 카테고리입니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}

