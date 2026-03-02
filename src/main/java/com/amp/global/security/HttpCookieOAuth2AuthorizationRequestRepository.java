package com.amp.global.security;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

/**
 * OAuth2 Authorization Request를 쿠키에 저장하는 Repository
 * STATELESS 세션 정책을 사용하면서 OAuth2 로그인을 지원하기 위함
 */
@Slf4j
@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180; // 3분

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        log.debug("Loading authorization request from cookie");
        return getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            log.debug("Authorization request is null, removing cookies");
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        log.info("Saving authorization request to cookie");
        log.debug("Authorization URI: {}", authorizationRequest.getAuthorizationUri());
        log.debug("Redirect URI: {}", authorizationRequest.getRedirectUri());
        log.debug("State: {}", authorizationRequest.getState());

        addCookie(response, request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);

        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            addCookie(response, request, REDIRECT_URI_PARAM_COOKIE_NAME,
                    redirectUriAfterLogin, COOKIE_EXPIRE_SECONDS);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        log.debug("Removing authorization request from cookie");
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(request, response);
        return authorizationRequest;
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request,
                                                  HttpServletResponse response) {
        deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        deleteCookie(request, response, REDIRECT_URI_PARAM_COOKIE_NAME);
        log.debug("Authorization request cookies removed");
    }

    private java.util.Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    log.debug("Found cookie: {}", name);
                    return java.util.Optional.of(cookie);
                }
            }
        }
        log.debug("Cookie not found: {}", name);
        return java.util.Optional.empty();
    }

    /**
     * OAuth2 인증 요청 쿠키 설정
     * - 프로덕션(HTTPS): SameSite=None; Secure; Domain=.ampnotice.kr
     *   → Google OAuth 콜백(cross-site)에서 반드시 쿠키가 전달되어야 함
     * - 로컬(HTTP): SameSite=Lax (SameSite=None은 Secure 없이 사용 불가)
     */
    private void addCookie(HttpServletResponse response, HttpServletRequest request,
                           String name, String value, int maxAge) {

        String serverName = request.getServerName();
        boolean isProdDomain = serverName != null && serverName.endsWith(".ampnotice.kr");
        boolean isHttps = isProdDomain || "https".equals(request.getScheme());

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .maxAge(maxAge);

        if (isHttps) {
            // 프로덕션: SameSite=None + Secure + Domain 으로 cross-site OAuth 콜백 보장
            // api.host.ampnotice.kr에서 설정한 쿠키가 api.ampnotice.kr 콜백에서도 전달되려면 필수
            builder.secure(true)
                    .sameSite("None")
                    .domain(".ampnotice.kr");
        } else {
            // 로컬: SameSite=None은 Secure 없이 불가하므로 Lax 사용
            builder.secure(false)
                    .sameSite("Lax");
        }

        ResponseCookie cookie = builder.build();
        response.addHeader("Set-Cookie", cookie.toString());
        log.debug("Added cookie: {} (maxAge: {}, secure: {}, sameSite: {}, serverName: {})",
                name, maxAge, isHttps, isHttps ? "None" : "Lax", serverName);
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                    log.debug("Deleted cookie: {}", name);
                }
            }
        }
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(authorizationRequest));
    }

    private OAuth2AuthorizationRequest deserialize(Cookie cookie) {
        try {
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(
                    Base64.getUrlDecoder().decode(cookie.getValue())
            );
        } catch (Exception e) {
            log.error("Failed to deserialize authorization request", e);
            return null;
        }
    }
}
