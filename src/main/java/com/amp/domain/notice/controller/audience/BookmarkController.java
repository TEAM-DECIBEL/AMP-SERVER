package com.amp.domain.notice.controller.audience;

import com.amp.domain.notice.dto.request.BookmarkRequest;
import com.amp.domain.notice.dto.response.BookmarkResponse;
import com.amp.domain.notice.service.audience.BookmarkService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@Tag(name = "Notice")
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Operation(summary = "공지 저장 및 해제", description = "공연의 공지 저장 및 저장 해제 api")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_UPDATE_BOOKMARK)
    @PostMapping("/notices/{noticeId}/bookmark")
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

}
