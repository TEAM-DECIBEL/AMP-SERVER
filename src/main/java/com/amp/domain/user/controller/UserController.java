package com.amp.domain.user.controller;

import com.amp.domain.user.dto.response.MyPageResponse;
import com.amp.domain.user.dto.response.SavedNoticesResponse;
import com.amp.domain.user.service.UserNoticesService;
import com.amp.domain.user.service.UserService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User API")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserNoticesService userNoticesService;

    @GetMapping("/me")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_GET_MY_PAGE)
    @Operation(summary = "마이페이지 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    public ResponseEntity<BaseResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long userId = principal.getUserId();
        MyPageResponse response = userService.getMyPage(userId);
        return ResponseEntity
                .status(SuccessStatus.USER_PROFILE_RETRIEVED.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.USER_PROFILE_RETRIEVED, response));
    }

    @GetMapping("/me/saved-notices")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_GET_BOOKMARK_NOTICE)
    @Operation(summary = "저장한 공지 조회", description = "사용자가 저장한 공지사항 목록을 조회합니다.")
    public ResponseEntity<BaseResponse<SavedNoticesResponse>> getSavedNotices(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Long userId = principal.getUserId();
        SavedNoticesResponse response = userNoticesService.getSavedAnnouncements(userId, page, size);
        return ResponseEntity
                .status(SuccessStatus.SAVED_NOTICES_RETRIEVED.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.SAVED_NOTICES_RETRIEVED, response));
    }

}