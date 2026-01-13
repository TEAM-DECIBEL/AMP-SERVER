package com.amp.domain.notice.controller;

import com.amp.domain.notice.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common/v1/notices")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(
            @Valid @RequestParam Long noticeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        NoticeDetailResponse response = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok(response);
    }


}
