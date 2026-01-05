package com.amp.global.swagger;

import com.amp.global.common.CommonErrorCode;
import com.amp.global.common.ErrorCode;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public enum SwaggerResponseDescription {

    /**
     * [예시 1] 입력값 검증 실패
     * @Valid 검증이나 타입 미스매치 시
     */
    BAD_REQUEST_EXAMPLE(new LinkedHashSet<>(Set.of(
            CommonErrorCode.INVALID_INPUT_VALUE,
            CommonErrorCode.TYPE_MISMATCH,
            CommonErrorCode.INVALID_JSON
    )));

    private final Set<ErrorCode> errorCodeList;

    SwaggerResponseDescription(Set<ErrorCode> errorCodes) {
        errorCodes.add(CommonErrorCode.INTERNAL_SERVER_ERROR);

        this.errorCodeList = errorCodes;
    }
}
