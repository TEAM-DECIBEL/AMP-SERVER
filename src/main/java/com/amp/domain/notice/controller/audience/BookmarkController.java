package com.amp.domain.notice.controller.audience;

import com.amp.domain.audience.dto.response.SavedNoticesResponse;
import com.amp.domain.audience.service.AudienceNoticesService;
import com.amp.domain.notice.dto.request.BookmarkRequest;
import com.amp.domain.notice.dto.response.BookmarkResponse;
import com.amp.domain.notice.service.audience.BookmarkService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@Tag(name = "Notice")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final AudienceNoticesService audienceNoticesService;

    @Operation(summary = "공지 북마크 업데이트")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_UPDATE_BOOKMARK)
    @PostMapping("/api/v1/notices/{noticeId}/bookmark")
    public ResponseEntity<BaseResponse<BookmarkResponse>> updateBookmark(
            @PathVariable @Positive Long noticeId,
            @RequestBody @Valid BookmarkRequest bookmarkRequest
    ) {
        BookmarkResponse response =
                bookmarkService.updateBookmark(noticeId, bookmarkRequest);

        return ResponseEntity
                .status(SuccessStatus.BOOKMARK_UPDATE_SUCCESS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.BOOKMARK_UPDATE_SUCCESS.getMsg(), response));
    }

    @GetMapping("/api/v1/bookmarks")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_GET_BOOKMARK_NOTICE)
    @Operation(summary = "저장한 공지 조회", description = "관객이 저장한 공지사항 목록을 조회합니다.")
    public ResponseEntity<BaseResponse<SavedNoticesResponse>> getSavedNotices(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Long userId = principal.getUserId();
        SavedNoticesResponse response = audienceNoticesService.getSavedAnnouncements(userId, page, size);
        return ResponseEntity
                .status(SuccessStatus.SAVED_NOTICES_RETRIEVED.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.SAVED_NOTICES_RETRIEVED, response));
    }
}
