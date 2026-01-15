package com.amp.global.exception;


import com.amp.global.common.CommonErrorCode;
import com.amp.global.common.ErrorCode;
import com.amp.global.response.error.BaseErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseErrorResponse> handlerCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.error("[ERROR - CustomException] Code: {}, Msg: {}", errorCode.getCode(), errorCode.getMsg());
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(BaseErrorResponse.of(errorCode));
    }

    // 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseErrorResponse> handlerInternalServerError(
            Exception ex,
            HttpServletRequest request
    ) {
        // 무중단 배포환경에서 health check를 위해 actuator 예외는 Spring 기본 처리로 넘김
        if (request.getRequestURI().startsWith("/actuator")) {
            throw new RuntimeException(ex);
        }

        log.error("[ERROR - Unknown Exception]", ex);
        return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(BaseErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }

    // 타입 불일치
    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<BaseErrorResponse> handleTypeMismatch(TypeMismatchException ex) {
        log.error("[ERROR - TypeMismatchException]", ex);
        return ResponseEntity
                .status(CommonErrorCode.TYPE_MISMATCH.getHttpStatus())
                .body(BaseErrorResponse.of(CommonErrorCode.TYPE_MISMATCH));
    }

    // JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<BaseErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("[ERROR - HttpMessageNotReadableException]", ex);
        return ResponseEntity.status(CommonErrorCode.INVALID_JSON.getHttpStatus())
                .body(BaseErrorResponse.of(CommonErrorCode.INVALID_JSON));
    }

    // @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("[ERROR - MethodArgumentNotValidException] {}", ex.getMessage());
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT_VALUE.getHttpStatus())
                .body(BaseErrorResponse.of(CommonErrorCode.INVALID_INPUT_VALUE));
    }
}
