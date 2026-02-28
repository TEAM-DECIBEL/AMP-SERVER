package com.amp.global.security;

import com.amp.domain.user.entity.UserType;
import com.amp.global.security.util.DomainRoleMapping;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Slf4j
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    private final DomainRoleMapping domainRoleMapping;

    public CustomOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository,
            DomainRoleMapping domainRoleMapping) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
        this.domainRoleMapping = domainRoleMapping;
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
        String userTypeParam = request.getParameter("userType");
        UserType requestedUserType = parseUserType(userTypeParam);

        // 2. Origin 추출 (API 서브도메인 제거)
        String origin = extractOrigin(request);
        String convertedOrigin = domainRoleMapping.convertToFrontendOrigin(origin, requestedUserType);

        log.info("Original origin: {}, Converted origin: {}", origin, convertedOrigin);

        // 3. 파라미터가 없거나 유효하지 않으면 Origin 기반으로 자동 감지
        if (requestedUserType == null) {
            requestedUserType = domainRoleMapping.getUserTypeFromOrigin(convertedOrigin);
        }

        // 4. 보안: 허용된 도메인인지 검증 (Open Redirect 방지)
        String safeOrigin = domainRoleMapping.getSafeOrigin(convertedOrigin, requestedUserType);

        log.info("OAuth2 authorization request with userType: {}, safeOrigin: {}", requestedUserType, safeOrigin);

        // state에 userType과 검증된 프론트엔드 origin 정보 추가
        String originalState = authorizationRequest.getState();
        String customState = originalState + "|userType=" + requestedUserType.name() + "|origin=" + safeOrigin;

        return OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .state(customState)
                .build();
    }

    /**
     * Request에서 Origin을 추출
     * 우선순위: Origin 헤더 > Referer 헤더 > Server Info
     */
    private String extractOrigin(HttpServletRequest request) {
        // 1. Origin 헤더 확인
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.trim().isEmpty()) {
            log.info("Origin extracted from Origin header: {}", origin);
            return origin;
        }

        // 2. Referer 헤더에서 추출
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.trim().isEmpty()) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                String extractedOrigin = uri.getScheme() + "://" + uri.getAuthority();
                log.info("Origin extracted from Referer header: {}", extractedOrigin);
                return extractedOrigin;
            } catch (Exception e) {
                log.warn("Failed to parse Referer header: {}", referer, e);
            }
        }

        // 3. Server Info 기반 (fallback)
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder fallbackOrigin = new StringBuilder();
        fallbackOrigin.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            fallbackOrigin.append(":").append(serverPort);
        }

        String result = fallbackOrigin.toString();
        log.info("Origin extracted from server info: {}", result);
        return result;
    }

    /**
     * 문자열을 UserType으로 파싱
     * @return 유효하면 UserType, 그렇지 않으면 null
     */
    private UserType parseUserType(String userType) {
        if (userType == null || userType.trim().isEmpty()) {
            return null;
        }
        try {
            return UserType.valueOf(userType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}