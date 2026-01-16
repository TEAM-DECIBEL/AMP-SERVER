package com.amp.api.auth.controller;

import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    @PostMapping("/logout")
    public BaseResponse<Void> logout() {
        log.info("User logout requested");
        return BaseResponse.of(SuccessStatus.LOGOUT_SUCCESS, null);
    }
}