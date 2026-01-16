package com.amp.domain.auth.controller;

import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class LogoutController {

    @PostMapping("/logout")
    public BaseResponse<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated())
        ? auth.getName(): "anonymous";
        log.info("User logout requested: {}", username);
        return BaseResponse.of(SuccessStatus.LOGOUT_SUCCESS, null);
    }
}