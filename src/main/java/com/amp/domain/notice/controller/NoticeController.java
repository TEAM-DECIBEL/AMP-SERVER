package com.amp.domain.notice.controller;

import com.amp.domain.notice.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.service.NoticeService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/common/v1/notices")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "공지 상세 조회")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_GET_NOTICE_DETAIL)
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(
            @PathVariable("noticeId") @Positive Long noticeId
    ) {
        NoticeDetailResponse response = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok(response);
    }


}
