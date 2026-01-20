package com.amp.domain.organizer.controller;

import com.amp.domain.organizer.dto.response.OrganizerMypageResponse;
import com.amp.domain.organizer.service.OrganizerMypageService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Organizer API")
@RestController
@RequestMapping("/api/v1/organizer/mypage")
@RequiredArgsConstructor
public class OrganizerMypageController {

    private final OrganizerMypageService organizerMypageService;

    @Operation(summary = "주최사 마이페이지 조회",
            description = "주최사의 프로필 정보와 공연 현황을 조회합니다.")
    @GetMapping
    public ResponseEntity<BaseResponse<OrganizerMypageResponse>> getOrganizerMypage(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        OrganizerMypageResponse response = organizerMypageService.getOrganizerMypage(userPrincipal.getUserId());
        return ResponseEntity
                .status(SuccessStatus.GET_ORGANIZER_MYPAGE_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.GET_ORGANIZER_MYPAGE_SUCCESS.getMsg(), response));
    }

}