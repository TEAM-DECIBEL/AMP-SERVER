package com.amp.domain.category.exception;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CategoryErrorCode implements ErrorCode {

    // 400 BAD REQUEST
    CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "CAT", "001", "한 개 이상의 카테고리 선택은 필수입니다."),

    // 404 NOT FOUND
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CAT", "001", "존재하지 않는 카테고리입니다.");

    private final HttpStatus httpStatus;
    private final String domain;
    private final String numbering;
    private final String msg;

    @Override
    public String getCode() {
        return domain + "_" + httpStatus.value() + "_" + numbering;
    }
}
