package com.amp.domain.notice.controller;

import com.amp.domain.notice.dto.request.NoticeSaveRequest;
import com.amp.domain.notice.dto.response.NoticeSaveResponse;
import com.amp.domain.notice.service.UserSavedNoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
@Slf4j
public class UserSavedNoticeController {

    private final UserSavedNoticeService userSavedNoticeService;

    // 공지 북마크 저장
    @PostMapping("/{noticeId}/bookmark")
    // TODO 북마크로 네이밍 변경 건의?
    public ResponseEntity<NoticeSaveResponse> saveNotice(
            @PathVariable("noticeId") Long noticeId,
            @RequestBody NoticeSaveRequest noticeSaveRequest
    ) {
        NoticeSaveResponse response =
                userSavedNoticeService.saveNotice(noticeId, noticeSaveRequest);

        return ResponseEntity.ok(response);
    }

}

