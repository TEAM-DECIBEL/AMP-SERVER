package com.amp.global.security;

import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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

        addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);

        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
            addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME,
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

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        // ✅ 프로덕션에서는 true로 설정
        cookie.setSecure(false); // 로컬 개발: false, 프로덕션: true

        response.addCookie(cookie);
        log.debug("Added cookie: {} (maxAge: {})", name, maxAge);
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