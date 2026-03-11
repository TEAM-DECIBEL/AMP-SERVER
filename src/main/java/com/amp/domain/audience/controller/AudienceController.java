package com.amp.domain.audience.controller;

import com.amp.domain.audience.dto.response.AudienceMyPageResponse;
import com.amp.domain.audience.dto.response.NicknameResponse;
import com.amp.domain.audience.dto.response.SavedNoticesResponse;
import com.amp.domain.audience.service.AudienceNoticesService;
import com.amp.domain.audience.service.AudienceService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import com.amp.global.security.service.AuthService;
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
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/audience")
@Tag(name = "Audience")
@Validated
@RequiredArgsConstructor
public class AudienceController {

    private final AudienceService audienceService;
    private final AuthService authService;
    private final AudienceNoticesService audienceNoticesService;

    @GetMapping("/mypage")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_GET_MY_PAGE)
    @Operation(summary = "마이페이지 조회", description = "현재 로그인한 관객의 프로필 정보 조회 api")
    @PreAuthorize("hasRole('AUDIENCE')")
    public ResponseEntity<BaseResponse<AudienceMyPageResponse>> getMyPage(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        AudienceMyPageResponse response = audienceService.getMyPage(principal.getEmail());
        return ResponseEntity
                .status(SuccessStatus.USER_PROFILE_RETRIEVED.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.USER_PROFILE_RETRIEVED, response));
    }

    @GetMapping("/nickname")
    @Operation(summary = "닉네임 조회", description = "로그인한 관객의 닉네임 반환, 미로그인 시 '관객' 반환 api")
    public ResponseEntity<BaseResponse<NicknameResponse>> getAudienceNickname(){
        NicknameResponse nicknameResponse = new NicknameResponse(authService.getDisplayNickname());
        return ResponseEntity
                .status(SuccessStatus.USER_NICKNAME_RETRIEVED.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.USER_NICKNAME_RETRIEVED, nicknameResponse));
    }

    @GetMapping("/bookmarks")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_GET_BOOKMARK_NOTICE)
    @Operation(summary = "저장한 공지 조회", description = "관객이 저장한 공지사항 목록 조회 api")
    @PreAuthorize("hasRole('AUDIENCE')")
    public ResponseEntity<BaseResponse<SavedNoticesResponse>> getSavedNotices(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "페이지 크기 (최대 100)")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        Long userId = principal.getUserId();
        SavedNoticesResponse response = audienceNoticesService.getSavedAnnouncements(userId, page, size);
        return ResponseEntity
                .status(SuccessStatus.SAVED_NOTICES_RETRIEVED.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.SAVED_NOTICES_RETRIEVED, response));
    }

}
