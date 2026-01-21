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

        // 요청 파라미터에서 userType 가져오기
        String userType = request.getParameter("userType");

        if (userType == null || !isValidUserType(userType)) {
            String serverName = request.getServerName();
            log.info("Detecting userType from domain: {}", serverName);

            if (serverName.equals("www.ampnotice-host.kr") || serverName.equals("ampnotice-host.kr")) {
                userType = "ORGANIZER";
                log.info("Domain matched organizer: {} -> userType: ORGANIZER", serverName);
            } else {
                // Referer에서 추출 시도
                String referer = request.getHeader("Referer");
                if (referer != null) {
                    if (referer.contains("/organizer")) {
                        userType = "ORGANIZER";
                    } else {
                        userType = "AUDIENCE";
                    }
                } else {
                    // 기본값: 관객
                    log.warn("userType parameter is missing. Defaulting to 'AUDIENCE'");
                    userType = "AUDIENCE";
                }
            }
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

    private boolean isValidUserType(String userType) {
        try {
            UserType.valueOf(userType);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}