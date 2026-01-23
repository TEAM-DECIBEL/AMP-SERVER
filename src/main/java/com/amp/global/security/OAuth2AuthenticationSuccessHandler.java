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

        // state에서 userType 추출 (이미 CustomOAuth2AuthorizationRequestResolver가 판단해서 넣어둠)
        String state = request.getParameter("state");
        UserType requestedUserType = extractUserTypeFromState(state);

        String token = jwtUtil.generateToken(email);
        log.info("New JWT token generated");

        addTokenCookie(response, token);

        log.info("Response committed after cookie: {}", response.isCommitted());

        String targetUrl = determineTargetUrl(request, user, requestedUserType, token);

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

    private String determineTargetUrl(HttpServletRequest request, User user,
                                      UserType requestedUserType, String token) {

        // Origin 기반으로 callback URI 생성
        String callbackUri = extractCallbackUri(request);

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

    /**
     * Origin 또는 Referer 헤더를 기반으로 callback URI를 동적으로 생성
     */
    private String extractCallbackUri(HttpServletRequest request) {
        // 1. Origin 헤더 확인 (가장 신뢰할 수 있는 소스)
        String origin = request.getHeader("Origin");

        if (origin != null && !origin.trim().isEmpty()) {
            String callbackUri = origin + "/callback";
            log.info("Callback URI extracted from Origin header: {}", callbackUri);
            return callbackUri;
        }

        // 2. Referer 헤더에서 추출 시도
        String referer = request.getHeader("Referer");

        if (referer != null && !referer.trim().isEmpty()) {
            try {
                // Referer에서 origin 부분만 추출
                java.net.URI uri = new java.net.URI(referer);
                String extractedOrigin = uri.getScheme() + "://" + uri.getAuthority();
                String callbackUri = extractedOrigin + "/callback";
                log.info("Callback URI extracted from Referer header: {}", callbackUri);
                return callbackUri;
            } catch (Exception e) {
                log.warn("Failed to parse Referer header: {}", referer, e);
            }
        }

        // 3. 요청의 서버 정보에서 추출 (fallback)
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder fallbackUri = new StringBuilder();
        fallbackUri.append(scheme).append("://");

        // www. 제거 (www.ampnotice-host.kr -> ampnotice-host.kr)
        if (serverName.startsWith("www.")) {
            serverName = serverName.substring(4);
        }

        fallbackUri.append(serverName);

        // 기본 포트가 아닌 경우에만 포트 추가
        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            fallbackUri.append(":").append(serverPort);
        }

        fallbackUri.append("/callback");

        String callbackUri = fallbackUri.toString();
        log.info("Callback URI extracted from request server info: {}", callbackUri);
        return callbackUri;
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