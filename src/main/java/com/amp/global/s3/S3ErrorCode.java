package com.amp.global.s3;

import com.amp.global.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum S3ErrorCode implements ErrorCode {
    // 400 Bad Request
    INVALID_PROFILE_IMAGE(HttpStatus.BAD_REQUEST, "S3_400_001","이미지 파일만 업로드 가능합니다."),
    FILE_NAME_NOT_FOUND(HttpStatus.NOT_FOUND, "S3_400_002", "파일명이 없습니다."),
    INVALID_DIRECTORY_ROUTE(HttpStatus.NOT_FOUND, "S3_400_003", "잘못된 디렉토리 경로입니다."),

    // 500 Internal Server Error
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_001", "S3 업로드를 실패하였습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String msg;
}
