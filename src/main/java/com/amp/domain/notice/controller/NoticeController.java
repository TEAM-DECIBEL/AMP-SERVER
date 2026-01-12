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
@RequestMapping("/api/auth/Announcement")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {

    private final NoticeService noticeService;

    // 공지 상세 조회 api
    @PostMapping("/complete")
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(
            @Valid @RequestParam Long noticeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        NoticeDetailResponse response = noticeService.getNoticeDetail(noticeId, userDetails);
        return ResponseEntity.ok(response);
    }


}
