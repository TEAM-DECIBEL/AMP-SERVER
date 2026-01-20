package com.amp.domain.notice.controller.common;

import com.amp.domain.notice.dto.response.NoticeListResponse;
import com.amp.domain.notice.service.common.FestivalNoticeService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/common/festivals")
@Tag(name = "User API")
@Tag(name = "Organizer API")
@RequiredArgsConstructor
@Validated
public class FestivalNoticeController {

    private final FestivalNoticeService festivalNoticeService;

    @Operation(summary = "공연별 공지 리스트 조회")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_NOTICE_LIST)
    @GetMapping("/{festivalId}/notices")
    public ResponseEntity<BaseResponse<NoticeListResponse>> getFestivalNotices(
            @PathVariable("festivalId") Long festivalId,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) int size
    ) {
        NoticeListResponse response = festivalNoticeService.getFestivalNoticeList(festivalId, page, size);
        return ResponseEntity
                .status(SuccessStatus.NOTICE_LIST_GET_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.NOTICE_LIST_GET_SUCCESS.getMsg(), response));
    }

}
