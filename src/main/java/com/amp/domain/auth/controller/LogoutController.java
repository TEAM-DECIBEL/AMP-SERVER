package com.amp.domain.auth.controller;

import com.amp.domain.auth.dto.AuthStatusResponse;
import com.amp.domain.user.entity.User;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.security.service.AuthService;
import com.amp.global.security.util.DomainRoleMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class LogoutController {

    private final DomainRoleMapping domainRoleMapping;
    private final AuthService authService;

    @Value("${app.jwt.cookie-name:accessToken}")
    private String cookieName;

    @Value("${app.jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    @Operation(summary = "로그아웃", description = "JWT 쿠키를 삭제하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
        log.info("User logout requested: {}", username);

        // Origin 헤더 기반으로 쿠키 도메인/secure 결정 (로그인 시와 동일한 방식)
        String origin = request.getHeader("Origin");
        String cookieDomain = domainRoleMapping.getCookieDomain(origin);
        boolean secure = domainRoleMapping.shouldCookieBeSecure(origin);

        // maxAge=0으로 JWT 쿠키 만료 처리
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.trim().isEmpty()) {
            cookieBuilder.domain(cookieDomain);
        }

        response.addHeader("Set-Cookie", cookieBuilder.build().toString());

        // SecurityContext 초기화
        SecurityContextHolder.clearContext();

        log.info("JWT 쿠키 삭제 완료 - user: {}, domain: {}", username,
                cookieDomain != null ? cookieDomain : "current domain");
        return BaseResponse.of(SuccessStatus.LOGOUT_SUCCESS, null);
    }

    @Operation(summary = "로그인 상태 확인", description = "현재 사용자의 로그인 여부 및 정보를 반환합니다.")
    @GetMapping("/status")
    public BaseResponse<AuthStatusResponse> getAuthStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!authService.isLoggedInUser(auth)) {
            log.debug("Auth status check: 비로그인 상태");
            return BaseResponse.of(SuccessStatus.AUTH_STATUS_RETRIEVED, AuthStatusResponse.loggedOut());
        }

        User user = authService.getCurrentUserOrNull();
        if (user == null) {
            log.debug("Auth status check: 토큰은 있으나 사용자 없음");
            return BaseResponse.of(SuccessStatus.AUTH_STATUS_RETRIEVED, AuthStatusResponse.loggedOut());
        }

        log.debug("Auth status check: 로그인 상태 - {}", user.getEmail());
        return BaseResponse.of(SuccessStatus.AUTH_STATUS_RETRIEVED,
                AuthStatusResponse.loggedIn(user.getEmail(), user.getUserType()));
    }
}