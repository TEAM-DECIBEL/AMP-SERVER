package com.amp.domain.notice.controller;

import com.amp.domain.notice.dto.request.NoticeCreateRequest;
import com.amp.domain.notice.dto.response.NoticeCreateResponse;
import com.amp.domain.notice.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.service.NoticeService;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common/v1/notices")
@RequiredArgsConstructor
@Validated
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 생성")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_CREATE_NOTICE)
    @PostMapping(path = "/{festivalId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<NoticeCreateResponse>> createNotice(
            @PathVariable("festivalId") @Positive Long festivalId,
            @ModelAttribute @Valid NoticeCreateRequest request) {
        NoticeCreateResponse response = noticeService.createNotice(festivalId, request);
        return ResponseEntity
                .status(SuccessStatus.NOTICE_CREATE_SUCCESS.getHttpStatus())
                .body(BaseResponse.create(SuccessStatus.NOTICE_CREATE_SUCCESS.getMsg(), response));
    }

    @Operation(summary = "공지 상세 조회")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_NOTICE_DETAIL)
    @GetMapping("/{noticeId}")
    public ResponseEntity<BaseResponse<NoticeDetailResponse>> getNoticeDetail(
            @PathVariable("noticeId") @Positive Long noticeId
    ) {
        NoticeDetailResponse response = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity
                .status(SuccessStatus.NOTICE_DETAIL_GET_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.NOTICE_DETAIL_GET_SUCCESS.getMsg(), response));
    }

    // 공지 삭제 API
    @Operation(summary = "공지 삭제")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_DELETE_NOTICE)
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<BaseResponse<Void>> deleteNotice(
            @PathVariable("noticeId") @Positive Long noticeId
    ) {
        noticeService.deleteNotice(noticeId);

        return ResponseEntity
                .status(SuccessStatus.NOTICE_DELETE_SUCCESS.getHttpStatus())
                .body(BaseResponse
                        .ok(SuccessStatus.NOTICE_DELETE_SUCCESS.getMsg(), null));
    }

}
