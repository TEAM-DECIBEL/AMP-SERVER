package com.amp.domain.notice.controller.user;

import com.amp.domain.notice.dto.request.BookmarkRequest;
import com.amp.domain.notice.dto.response.BookmarkResponse;
import com.amp.domain.notice.service.user.BookmarkService;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/notices")
@Tag(name = "User API")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // 공지 북마크 업데이트
    @Operation(summary = "공지 북마크 업데이트")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_UPDATE_BOOKMARK)
    @PostMapping("/{noticeId}/bookmark")
    public ResponseEntity<BaseResponse<BookmarkResponse>> updateBookmark(
            @PathVariable("noticeId") @Positive Long noticeId,
            @RequestBody @Valid BookmarkRequest bookmarkRequest
    ) {
        BookmarkResponse response =
                bookmarkService.updateBookmark(noticeId, bookmarkRequest);

        return ResponseEntity
                .status(SuccessStatus.BOOKMARK_UPDATE_SUCCESS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.BOOKMARK_UPDATE_SUCCESS.getMsg(), response));
    }

}

