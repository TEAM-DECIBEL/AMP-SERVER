package com.amp.global.security;

import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri.audience:http://localhost:5173/auth/callback}")
    private String audienceRedirectUri;

    @Value("${app.oauth2.redirect-uri.organizer:http://localhost:5174/auth/callback}")
    private String organizerRedirectUri;

    @Value("${app.oauth2.onboarding-uri.audience:http://localhost:5173/onboarding}")
    private String audienceOnboardingUri;

    @Value("${app.oauth2.onboarding-uri.organizer:http://localhost:5174/onboarding}")
    private String organizerOnboardingUri;

    @Value("${app.jwt.cookie-name:accessToken}")
    private String cookieName;

    @Value("${app.jwt.cookie-max-age:3600}") // 1시간 (초 단위)
    private int cookieMaxAge;

    @Value("${app.jwt.cookie-domain:localhost}")
    private String cookieDomain;

    @Value("${app.jwt.cookie-secure:true}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 login"));

        // state에서 userType 추출
        String state = request.getParameter("state");
        UserType userType = extractUserTypeFromState(state);

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(email);

        // 쿠키에 토큰 설정
        addTokenCookie(response, token);

        String targetUrl;

        // 온보딩이 필요한 경우
        if (user.getRegistrationStatus() == RegistrationStatus.PENDING) {
            user.updateUserType(userType);
            userRepository.save(user);

            // UserType에 따라 다른 온보딩 URL 선택
            String baseOnboardingUri = (userType == UserType.ORGANIZER)
                    ? organizerOnboardingUri
                    : audienceOnboardingUri;

            targetUrl = UriComponentsBuilder.fromUriString(baseOnboardingUri)
                    .queryParam("userType", userType.name())
                    .queryParam("status", "pending")
                    .build().toUriString();

            log.info("Redirecting to onboarding ({}): {}", userType, targetUrl);
        }
        // 온보딩 완료된 경우
        else {
            // UserType에 따라 다른 메인 URL 선택
            String baseRedirectUri = (user.getUserType() == UserType.ORGANIZER)
                    ? organizerRedirectUri
                    : audienceRedirectUri;

            targetUrl = UriComponentsBuilder.fromUriString(baseRedirectUri)
                    .queryParam("status", "completed")
                    .build().toUriString();

            log.info("Redirecting to main app ({}): {}", user.getUserType(), targetUrl);
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(cookieName, token);
        cookie.setHttpOnly(true); // XSS 공격 방지
        cookie.setSecure(cookieSecure); // HTTPS에서만 전송 (프로덕션: true, 로컬: false)
        cookie.setPath("/"); // 모든 경로에서 접근 가능
        cookie.setMaxAge(cookieMaxAge); // 쿠키 유효기간

        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain); // 도메인 설정 (필요시)
        }

        // SameSite 설정 (CSRF 방지)
        // Spring Boot 3.x 이상에서는 ResponseCookie 사용 권장
        response.addCookie(cookie);

        log.info("JWT token added to cookie: {}", cookieName);
    }

    private UserType extractUserTypeFromState(String state) {
        if (state == null) {
            log.warn("State parameter is null, defaulting to AUDIENCE");
            return UserType.AUDIENCE;
        }

        try {
            if (state.contains("|userType=")) {
                String[] parts = state.split("\\|userType=");
                if (parts.length == 2) {
                    String userTypeStr = parts[1];
                    return UserType.valueOf(userTypeStr);
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse userType from state: {}", state, e);
        }

        log.warn("Could not extract userType from state, defaulting to AUDIENCE");
        return UserType.AUDIENCE;
    }
}