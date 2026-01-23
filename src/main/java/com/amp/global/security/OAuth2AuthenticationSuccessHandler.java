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

    @Value("${app.jwt.cookie-name:accessToken}")
    private String cookieName;

    @Value("${app.jwt.cookie-max-age:3600}")
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

        addTokenCookie(response, token);

        log.info("Response committed after cookie: {}", response.isCommitted());

        String targetUrl = determineTargetUrl(clientOrigin, user, requestedUserType, token);

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

    private String determineTargetUrl(String clientOrigin, User user,
                                      UserType requestedUserType, String token) {

        // state에서 추출한 origin을 callback URI로 사용
        String callbackUri = clientOrigin + "/callback";
        log.info("Using callback URI from state: {}", callbackUri);

        if (user.getRegistrationStatus() == RegistrationStatus.PENDING) {
            // 온보딩 필요
            user.updateUserType(requestedUserType);
            userRepository.save(user);
            log.info("Updated user type to {} for pending user: {}", requestedUserType, user.getEmail());

            return UriComponentsBuilder.fromUriString(callbackUri)
                    .queryParam("token", token)
                    .queryParam("status", "PENDING")
                    .build().toUriString();
        } else {
            // 온보딩 완료
            log.info("User registration completed, redirecting to home: {}", user.getEmail());

            return UriComponentsBuilder.fromUriString(callbackUri)
                    .queryParam("token", token)
                    .queryParam("status", "COMPLETED")
                    .build().toUriString();
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