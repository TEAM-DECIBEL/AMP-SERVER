package com.amp.domain.notice.organizer.controller;

import com.amp.domain.notice.organizer.dto.request.NoticeUpdateRequest;
import com.amp.domain.notice.organizer.service.NoticeUpdateService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizer/notices")
@RequiredArgsConstructor
public class NoticeUpdateController {
    private final NoticeUpdateService noticeUpdateService;

    // 공지 수정/상단고정 수정
    @Operation(summary = "공지 수정/상단고정")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_UPDATE_NOTICE)
    @PutMapping(path = "/{noticeId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<Void>> updateNotice(
            @PathVariable("noticeId") @Positive Long noticeId,
            @ModelAttribute @Valid NoticeUpdateRequest noticeUpdateRequest
    ) {
        noticeUpdateService.updateNotice(noticeId, noticeUpdateRequest);

        return ResponseEntity
                .status(SuccessStatus.UPDATE_NOTICE_SUCCESS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.UPDATE_NOTICE_SUCCESS.getMsg(), null));
    }

}

