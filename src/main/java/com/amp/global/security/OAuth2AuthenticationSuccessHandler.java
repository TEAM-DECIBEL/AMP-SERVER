package com.amp.global.security;

import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.security.util.DomainRoleMapping;
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
    private final DomainRoleMapping domainRoleMapping;

    @Value("${app.jwt.cookie-name:accessToken}")
    private String cookieName;

    @Value("${app.jwt.cookie-max-age:3600}")
    private int cookieMaxAge;

    @Value("${app.jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        log.info("=== OAuth2 인증 성공 ===");

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

        // state에서 userType과 origin 추출
        String state = request.getParameter("state");
        UserType requestedUserType = extractUserTypeFromState(state);
        String clientOrigin = extractOriginFromState(state);

        log.info("Extracted from state - userType: {}, origin: {}", requestedUserType, clientOrigin);

        String token = jwtUtil.generateToken(email);
        log.info("New JWT token generated");

        // 쿠키에 토큰 설정 (URL에는 토큰 노출하지 않음)
        addTokenCookie(response, token, clientOrigin);

        log.info("Response committed after cookie: {}", response.isCommitted());

        String targetUrl = determineTargetUrl(clientOrigin, user, requestedUserType);

        log.info("Final redirect URL: {}", targetUrl);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void addTokenCookie(HttpServletResponse response, String token, String origin) {
        // origin 기반으로 쿠키 설정 결정
        String dynamicCookieDomain = domainRoleMapping.getCookieDomain(origin);
        boolean dynamicCookieSecure = domainRoleMapping.shouldCookieBeSecure(origin);

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(dynamicCookieSecure)
                .path("/")
                .maxAge(Duration.ofSeconds(cookieMaxAge))
                .sameSite(cookieSameSite);

        if (dynamicCookieDomain != null && !dynamicCookieDomain.trim().isEmpty()) {
            cookieBuilder.domain(dynamicCookieDomain);
            log.info("Setting cookie domain: {}", dynamicCookieDomain);
        } else {
            log.info("Cookie domain not set (will use current domain)");
        }

        ResponseCookie cookie = cookieBuilder.build();
        response.addHeader("Set-Cookie", cookie.toString());

        log.info("JWT token added to cookie - name: {}, secure: {}, sameSite: {}, maxAge: {}s, domain: {}",
                cookieName, dynamicCookieSecure, cookieSameSite, cookieMaxAge,
                dynamicCookieDomain != null && !dynamicCookieDomain.trim().isEmpty() ? dynamicCookieDomain : "current domain");
    }

    private String determineTargetUrl(String clientOrigin, User user,
                                      UserType requestedUserType) {

        if (user.getRegistrationStatus() == RegistrationStatus.PENDING) {
            // 신규 사용자: 온보딩 페이지로 직접 리다이렉트
            user.updateUserType(requestedUserType);
            userRepository.save(user);
            log.info("Updated user type to {} for pending user: {}", requestedUserType, user.getEmail());

            String onboardingUrl = clientOrigin + "/onboarding";
            log.info("New user, redirecting to onboarding: {}", onboardingUrl);

            return onboardingUrl;
        } else {
            // 기존 사용자: 도메인-역할 검증
            UserType actualUserType = user.getUserType();

            if (!domainRoleMapping.isValidDomainForRole(actualUserType, clientOrigin)) {
                // 도메인 불일치: 올바른 도메인의 메인 페이지로 리다이렉트
                String correctDomain = domainRoleMapping.getCorrectDomain(actualUserType, clientOrigin);

                log.info("Domain mismatch! User {} with type {} accessed from {}. Redirecting to {}",
                        user.getEmail(), actualUserType, clientOrigin, correctDomain);

                return correctDomain;
            }

            // 도메인 일치: 메인 페이지로 리다이렉트
            log.info("User registration completed, redirecting to main: {}", clientOrigin);

            return clientOrigin;
        }
    }

    private String extractOriginFromState(String state) {
        if (state == null) {
            log.warn("State parameter is null, using fallback origin");
            return "http://localhost:5173"; // fallback
        }

        try {
            if (state.contains("|origin=")) {
                String[] parts = state.split("\\|origin=");
                if (parts.length == 2) {
                    String origin = parts[1];
                    log.info("Extracted origin from state: {}", origin);
                    return origin;
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse origin from state: {}", state, e);
        }

        log.warn("Could not extract origin from state, using fallback");
        return "http://localhost:5173"; // fallback
    }

    private UserType extractUserTypeFromState(String state) {
        if (state == null) {
            log.warn("State parameter is null, defaulting to AUDIENCE");
            return UserType.AUDIENCE;
        }

        try {
            if (state.contains("|userType=")) {
                // origin도 포함되어 있으므로 정확히 파싱
                String[] parts = state.split("\\|");
                for (String part : parts) {
                    if (part.startsWith("userType=")) {
                        String userTypeStr = part.substring("userType=".length());
                        UserType userType = UserType.valueOf(userTypeStr);
                        log.info("Extracted userType from state: {}", userType);
                        return userType;
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse userType from state: {}", state, e);
        }

        log.warn("Could not extract userType from state, defaulting to AUDIENCE");
        return UserType.AUDIENCE;
    }
}