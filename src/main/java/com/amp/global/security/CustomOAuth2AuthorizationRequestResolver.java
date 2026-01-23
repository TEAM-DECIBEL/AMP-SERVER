package com.amp.global.security;

import com.amp.domain.user.entity.UserType;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Slf4j
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest =
                defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest, request);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request) {

        if (authorizationRequest == null) {
            return null;
        }

        // 1. 요청 파라미터에서 userType 확인 (명시적으로 지정된 경우)
        String userType = request.getParameter("userType");

        // 2. 파라미터가 없거나 유효하지 않으면 Origin/Referer/Domain 기반으로 자동 감지
        if (userType == null || !isValidUserType(userType)) {
            userType = detectUserTypeFromRequest(request);
        }

        log.info("OAuth2 authorization request with userType: {}", userType);

        // state에 userType 정보 추가
        String originalState = authorizationRequest.getState();
        String customState = originalState + "|userType=" + userType;

        return OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .state(customState)
                .build();
    }

    /**
     * Request에서 UserType을 자동 감지
     * 우선순위: Origin 헤더 > Referer 헤더 > Server Name
     */
    private String detectUserTypeFromRequest(HttpServletRequest request) {
        // 1. Origin 헤더 확인
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.trim().isEmpty()) {
            String userType = determineUserTypeFromUrl(origin);
            log.info("UserType detected from Origin header: {} -> {}", origin, userType);
            return userType;
        }

        // 2. Referer 헤더 확인
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.trim().isEmpty()) {
            String userType = determineUserTypeFromUrl(referer);
            log.info("UserType detected from Referer header: {} -> {}", referer, userType);
            return userType;
        }

        // 3. Server Name 확인 (fallback)
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String serverInfo = serverName + ":" + serverPort;

        String userType = determineUserTypeFromUrl(serverInfo);
        log.info("UserType detected from server info: {} -> {}", serverInfo, userType);
        return userType;
    }

    /**
     * URL 문자열에서 UserType 판단
     * - localhost:5174 또는 ampnotice-host.kr -> ORGANIZER
     * - 그 외 -> AUDIENCE
     */
    private String determineUserTypeFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return "AUDIENCE";
        }

        String lowerUrl = url.toLowerCase();

        // ORGANIZER 조건
        if (lowerUrl.contains("localhost:5174") ||
                lowerUrl.contains("ampnotice-host.kr") ||
                lowerUrl.contains("www.ampnotice-host.kr")) {
            return "ORGANIZER";
        }

        // 기본값은 AUDIENCE
        return "AUDIENCE";
    }

    private boolean isValidUserType(String userType) {
        try {
            UserType.valueOf(userType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}