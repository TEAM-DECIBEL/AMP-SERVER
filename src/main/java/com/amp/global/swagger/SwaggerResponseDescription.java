package com.amp.global.swagger;

import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.global.common.CommonErrorCode;
import com.amp.global.common.ErrorCode;
import lombok.Getter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public enum SwaggerResponseDescription {

    /**
     * [예시 1] 입력값 검증 실패
     *
     * @Valid 검증이나 타입 미스매치 시
     */
    BAD_REQUEST_EXAMPLE(new LinkedHashSet<>(Set.of(
            CommonErrorCode.INVALID_INPUT_VALUE,
            CommonErrorCode.TYPE_MISMATCH,
            CommonErrorCode.INVALID_JSON
    ))),

    // 공연 추가 API
    FAIL_TO_CREATE_FESTIVAL(new LinkedHashSet<>(Set.of(
            FestivalErrorCode.INVALID_FESTIVAL_PERIOD,
            FestivalErrorCode.FESTIVAL_CREATE_FAILED,
            FestivalErrorCode.SCHEDULES_REQUIRED,
            FestivalErrorCode.INVALID_STAGE_FORMAT,
            FestivalErrorCode.INVALID_CATEGORY_FORMAT,
            FestivalErrorCode.MISSING_MAIN_IMAGE,
            FestivalErrorCode.INVALID_SCHEDULE_FORMAT
    ))),

    // 공지 상세 조회 API
    FAIL_TO_GET_NOTICE_DETAIL(new LinkedHashSet<>(Set.of(
            NoticeErrorCode.NOTICE_NOT_FOUND
    )));

    private final Set<ErrorCode> errorCodeList;

    SwaggerResponseDescription(Set<ErrorCode> errorCodes) {
        errorCodes.add(CommonErrorCode.INTERNAL_SERVER_ERROR);

        this.errorCodeList = errorCodes;
    }
}
