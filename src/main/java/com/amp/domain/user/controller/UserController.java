package com.amp.domain.user.controller;


import com.amp.domain.user.dto.response.MyPageResponse;
import com.amp.domain.user.service.UserService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long userId = principal.getUserId();
        MyPageResponse response = userService.getMyPage(userId);
        return ResponseEntity
                .status(SuccessStatus.OK.getHttpStatus())
                .body(BaseResponse.of(SuccessStatus.OK, response));
    }
}
