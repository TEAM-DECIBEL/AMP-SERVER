package com.amp.global.security;

import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Value("${app.oauth2.onboarding-uri.audience:http://localhost:5173/onboarding}")
    private String audienceOnboardingUri;

    @Value("${app.oauth2.onboarding-uri.organizer:http://localhost:5174/onboarding}")
    private String organizerOnboardingUri;

    @Value("${app.oauth2.home-uri.audience:http://localhost:5173/root}")
    private String audienceHomeUri;

    @Value("${app.oauth2.home-uri.organizer:http://localhost:5174/root}")
    private String organizerHomeUri;

    @Value("${app.jwt.cookie-name:accessToken}")
    private String cookieName;

    @Value("${app.jwt.cookie-max-age:3600}") // 1시간 (초 단위)
    private int cookieMaxAge;

    @Value("${app.jwt.cookie-domain:localhost}")
    private String cookieDomain;

    @Value("${app.jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("=== OAuth2 인증 성공 ===");

        // ✅ OAuth2 관련 쿠키 정리
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        log.info("OAuth2 User Email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found after OAuth2 login: {}", email);
                    return new IllegalStateException("User not found after OAuth2 login");
                });

        log.info("User found - ID: {}, Email: {}, Status: {}, UserType: {}",
                user.getId(), user.getEmail(), user.getRegistrationStatus(), user.getUserType());

        // state에서 userType 추출
        String state = request.getParameter("state");
        UserType requestedUserType = extractUserTypeFromState(state);

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(email);
        log.info("New JWT token generated");

        // 쿠키에 토큰 설정 시도 (cross-domain에서는 작동 안 함)
        addTokenCookie(response, token);

        log.info("Response committed after cookie: {}", response.isCommitted());

        String targetUrl = determineTargetUrl(user, requestedUserType, token);

        log.info("Final redirect URL: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(cookieMaxAge))
                .sameSite(cookieSameSite);

        if (cookieDomain != null && !cookieDomain.trim().isEmpty()) {
            cookieBuilder.domain(cookieDomain);
            log.info("Setting cookie domain: {}", cookieDomain);
        } else {
            log.info("Cookie domain not set (will use current domain)");
        }

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader("Set-Cookie", cookie.toString());

        log.info("JWT token added to cookie - name: {}, secure: {}, sameSite: {}, maxAge: {}s, domain: {}",
                cookieName, cookieSecure, cookieSameSite, cookieMaxAge,
                cookieDomain != null && !cookieDomain.trim().isEmpty() ? cookieDomain : "current domain");
    }

    private String determineTargetUrl(User user, UserType requestedUserType, String token) {
        if (user.getRegistrationStatus() == RegistrationStatus.PENDING) {
            // 온보딩 필요
            user.updateUserType(requestedUserType);
            userRepository.save(user);
            log.info("Updated user type to {} for pending user: {}", requestedUserType, user.getEmail());

            String onboardingUri = (requestedUserType == UserType.ORGANIZER)
                    ? organizerOnboardingUri
                    : audienceOnboardingUri;

            return UriComponentsBuilder.fromUriString(onboardingUri)
                    .queryParam("token", token)
                    .build().toUriString();
        } else {
            // 온보딩 완료
            log.info("User registration completed, redirecting to home: {}", user.getEmail());

            String homeUri = (user.getUserType() == UserType.ORGANIZER)
                    ? organizerHomeUri
                    : audienceHomeUri;

            return UriComponentsBuilder.fromUriString(homeUri)
                    .queryParam("token", token)
                    .build().toUriString();
        }
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
                    UserType userType = UserType.valueOf(userTypeStr);
                    log.info("Extracted userType from state: {}", userType);
                    return userType;
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse userType from state: {}", state, e);
        }

        log.warn("Could not extract userType from state, defaulting to AUDIENCE");
        return UserType.AUDIENCE;
    }
}