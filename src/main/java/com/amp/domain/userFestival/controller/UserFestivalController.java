package com.amp.domain.userFestival.controller;

import com.amp.domain.userFestival.dto.RecentFestivalResponse;
import com.amp.domain.userFestival.service.UserFestivalService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/festivals")
@RequiredArgsConstructor
public class UserFestivalController {

    private final UserFestivalService userFestivalService;

    @GetMapping("/recent")
    public ResponseEntity<BaseResponse<RecentFestivalResponse>> getRecentFestival(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        RecentFestivalResponse response = userFestivalService.getRecentFestival(userId);

        return ResponseEntity
                .status(SuccessStatus.USER_FESTIVAL_RECENT_FOUND.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.USER_FESTIVAL_RECENT_FOUND, response));
    }
}
