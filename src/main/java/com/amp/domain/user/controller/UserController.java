package com.amp.domain.user.controller;

import com.amp.domain.user.dto.response.MyPageResponse;
import com.amp.domain.user.dto.response.SavedAnnouncementResponse;
import com.amp.domain.user.service.UserAnnouncementService;
import com.amp.domain.user.service.UserService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 데이터 API")
public class UserController {

    private final UserService userService;
    private final UserAnnouncementService userAnnouncementService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long userId = principal.getUserId();
        MyPageResponse response = userService.getMyPage(userId);
        return ResponseEntity
                .status(SuccessStatus.OK.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.OK, response));
    }

    @GetMapping("/me/saved-announcements")
    @Operation(summary = "저장한 공지 조회", description = "사용자가 저장한 공지사항 목록을 조회합니다.")
    public ResponseEntity<BaseResponse<SavedAnnouncementResponse>> getSavedAnnouncements(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = principal.getUserId();
        SavedAnnouncementResponse response = userAnnouncementService.getSavedAnnouncements(userId, page, size);
        return ResponseEntity
                .status(SuccessStatus.OK.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.OK, response));
    }
}