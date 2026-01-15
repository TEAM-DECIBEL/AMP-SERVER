package com.amp.global.swagger;

import com.amp.domain.category.exception.FestivalCategoryErrorCode;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.notice.exception.BookmarkErrorCode;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.global.common.CommonErrorCode;
import com.amp.global.common.ErrorCode;
import com.amp.global.s3.S3ErrorCode;
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
    ))),

    // 공연 수정 API
    FAIL_TO_UPDATE_FESTIVAL(new LinkedHashSet<>(Set.of(
            FestivalErrorCode.INVALID_FESTIVAL_PERIOD,
            FestivalErrorCode.SCHEDULES_REQUIRED,
            FestivalErrorCode.INVALID_STAGE_FORMAT,
            FestivalErrorCode.INVALID_CATEGORY_FORMAT,
            FestivalErrorCode.INVALID_SCHEDULE_FORMAT,
            FestivalErrorCode.FESTIVAL_NOT_FOUND
    ))),

    // 공연 상세 정보 조회 API
    FAIL_TO_GET_FESTIVAL_DETAIL(new LinkedHashSet<>(Set.of(
            FestivalErrorCode.FESTIVAL_NOT_FOUND
    ))),

    // 공연 삭제 API
    FAIL_TO_DELETE_FESTIVAL(new LinkedHashSet<>(Set.of(
            FestivalErrorCode.FESTIVAL_NOT_FOUND,
            NoticeErrorCode.NOTICE_NOT_FOUND
    ))),

    // 공지 북마크 업데이트 API
    FAIL_TO_UPDATE_BOOKMARK(new LinkedHashSet<>(Set.of(
            NoticeErrorCode.NOTICE_NOT_FOUND,
            BookmarkErrorCode.NOTICE_ALREADY_BOOKMARKED,
            BookmarkErrorCode.SAVED_NOTICE_NOT_EXIST
    ))),

    // 공지 작성 API
    FAIL_TO_CREATE_NOTICE(new LinkedHashSet<>(Set.of(
            UserErrorCode.USER_NOT_FOUND,
            FestivalErrorCode.FESTIVAL_NOT_FOUND,
            UserErrorCode.USER_NOT_AUTHENTICATED,
            FestivalCategoryErrorCode.NOTICE_CATEGORY_NOT_FOUND,
            NoticeErrorCode.NOTICE_CREATE_FAILED,
            S3ErrorCode.S3_UPLOAD_FAILED
    ))),

    // 공지 삭제 API
    FAIL_TO_DELETE_NOTICE(new LinkedHashSet<>(Set.of(
            NoticeErrorCode.NOTICE_NOT_FOUND,
            NoticeErrorCode.NOTICE_ALREADY_DELETED,
            NoticeErrorCode.DELETE_NOTICE_FAIL,
            UserErrorCode.USER_NOT_FOUND,
            NoticeErrorCode.NOTICE_DELETE_FORBIDDEN
    ))),;

    private final Set<ErrorCode> errorCodeList;

    SwaggerResponseDescription(Set<ErrorCode> errorCodes) {
        errorCodes.add(CommonErrorCode.INTERNAL_SERVER_ERROR);

        this.errorCodeList = errorCodes;
    }
}
