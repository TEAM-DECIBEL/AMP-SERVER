package com.amp.domain.audience.controller;

import com.amp.domain.audience.dto.response.AudienceMyPageResponse;
import com.amp.domain.audience.dto.response.NicknameResponse;
import com.amp.domain.audience.service.AudienceService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import com.amp.global.security.service.AuthService;
import com.amp.global.swagger.SwaggerResponseDescription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User")
@Validated
@RequiredArgsConstructor
public class AudienceController {

    private final AudienceService audienceService;
    private final AuthService authService;

    @GetMapping("/mypage")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_GET_MY_PAGE)
    @Operation(summary = "마이페이지 조회", description = "현재 로그인한 관객의 프로필 정보를 조회합니다.")
    public ResponseEntity<BaseResponse<AudienceMyPageResponse>> getMyPage(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long userId = principal.getUserId();
        AudienceMyPageResponse response = audienceService.getMyPage(userId);
        return ResponseEntity
                .status(SuccessStatus.USER_PROFILE_RETRIEVED.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.USER_PROFILE_RETRIEVED, response));
    }

    @GetMapping("/nickname")
    @Operation(summary = "닉네임 조회", description = "로그인한 관객의 닉네임 반환, 미로그인 시 '관객' 반환")
    public ResponseEntity<BaseResponse<NicknameResponse>> getAudienceNickname(){
        NicknameResponse nicknameResponse = new NicknameResponse(authService.getDisplayNickname());
        return ResponseEntity
                .status(SuccessStatus.USER_NICKNAME_RETRIEVED.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.USER_NICKNAME_RETRIEVED, nicknameResponse));
    }

}
