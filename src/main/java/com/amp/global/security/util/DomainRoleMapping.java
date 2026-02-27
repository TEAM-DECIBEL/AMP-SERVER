package com.amp.global.security.util;

import com.amp.domain.user.entity.UserType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

import static com.amp.global.security.util.DomainConstants.*;

/**
 * 도메인-역할 매핑 유틸리티
 * <p>
 * 도메인 매핑:
 * - localhost:5173, ampnotice.kr → AUDIENCE
 * - localhost:5174, host.ampnotice.kr → ORGANIZER
 */
@Slf4j
@Component
public class DomainRoleMapping {

    // 도메인 → UserType 매핑
    private static final Map<String, UserType> DOMAIN_TO_ROLE = Map.of(
            LOCAL_AUDIENCE_HOST, UserType.AUDIENCE,
            LOCAL_ORGANIZER_HOST, UserType.ORGANIZER,
            PROD_AUDIENCE_HOST, UserType.AUDIENCE,
            PROD_AUDIENCE_WWW_HOST, UserType.AUDIENCE,
            PROD_ORGANIZER_HOST, UserType.ORGANIZER
    );

    // UserType → 로컬 도메인 매핑
    private static final Map<UserType, String> ROLE_TO_LOCAL_DOMAIN = Map.of(
            UserType.AUDIENCE, LOCAL_AUDIENCE_URL,
            UserType.ORGANIZER, LOCAL_ORGANIZER_URL
    );

    // UserType → 프로덕션 도메인 매핑
    private static final Map<UserType, String> ROLE_TO_PROD_DOMAIN = Map.of(
            UserType.AUDIENCE, PROD_AUDIENCE_URL,
            UserType.ORGANIZER, PROD_ORGANIZER_URL
    );

    /**
     * Origin에서 UserType을 추출
     *
     * @param origin 요청 origin (예: "http://localhost:5173", "https://host.ampnotice.kr")
     * @return 해당 도메인에 매핑된 UserType, 없으면 AUDIENCE 반환
     */
    public UserType getUserTypeFromOrigin(String origin) {
        if (origin == null || origin.trim().isEmpty()) {
            log.warn("Origin is null or empty, defaulting to AUDIENCE");
            return UserType.AUDIENCE;
        }

        String host = extractHost(origin);
        UserType userType = DOMAIN_TO_ROLE.getOrDefault(host, UserType.AUDIENCE);
        log.debug("Origin: {} -> Host: {} -> UserType: {}", origin, host, userType);
        return userType;
    }

    /**
     * UserType과 요청 origin에 따라 올바른 도메인 반환
     *
     * @param userType      사용자 역할
     * @param currentOrigin 현재 요청 origin
     * @return 해당 UserType에 맞는 도메인
     */
    public String getCorrectDomain(UserType userType, String currentOrigin) {
        boolean isProduction = isProductionOrigin(currentOrigin);

        if (isProduction) {
            return ROLE_TO_PROD_DOMAIN.get(userType);
        } else {
            return ROLE_TO_LOCAL_DOMAIN.get(userType);
        }
    }

    /**
     * 사용자 역할과 접근 도메인이 일치하는지 검증
     *
     * @param userType 사용자의 실제 역할
     * @param origin   접근 시도한 origin
     * @return 일치하면 true, 불일치하면 false
     */
    public boolean isValidDomainForRole(UserType userType, String origin) {
        UserType expectedType = getUserTypeFromOrigin(origin);
        boolean isValid = userType == expectedType;

        if (!isValid) {
            log.info("Domain-Role mismatch: userType={}, origin={}, expectedType={}",
                    userType, origin, expectedType);
        }

        return isValid;
    }

    /**
     * Origin 기반으로 쿠키 도메인 결정
     * - 프로덕션: .ampnotice.kr (서브도메인 공유)
     * - 로컬: null (현재 도메인 사용)
     *
     * @param origin 요청 origin
     * @return 쿠키에 설정할 도메인, 로컬 환경에서는 null
     */
    public String getCookieDomain(String origin) {
        if (isProductionOrigin(origin)) {
            return PROD_COOKIE_DOMAIN;
        }
        return null; // 로컬에서는 도메인 설정하지 않음
    }

    /**
     * 프로덕션 환경인지 확인
     *
     * @param origin 요청 origin
     * @return 프로덕션이면 true
     */
    public boolean isProductionOrigin(String origin) {
        if (origin == null) {
            return false;
        }
        String host = extractHost(origin);
        if (host == null) {
            return false;
        }
        String lowerHost = host.toLowerCase();
        return lowerHost.equals(PROD_AUDIENCE_HOST)
                || lowerHost.equals(PROD_AUDIENCE_WWW_HOST)
                || lowerHost.equals(PROD_ORGANIZER_HOST)
                || lowerHost.endsWith(PROD_COOKIE_DOMAIN);
    }

    /**
     * 쿠키 Secure 플래그 결정
     * - 프로덕션: true (HTTPS 필수)
     * - 로컬: false
     *
     * @param origin 요청 origin
     * @return Secure 플래그 값
     */
    public boolean shouldCookieBeSecure(String origin) {
        return isProductionOrigin(origin);
    }

    /**
     * API 도메인을 프론트엔드 도메인으로 변환
     * - https://api.ampnotice.kr → https://ampnotice.kr
     * - https://api.host.ampnotice.kr → https://host.ampnotice.kr
     * - http://localhost:8080 → origin 기반으로 결정
     *
     * @param origin            API origin
     * @param requestedUserType 요청한 UserType (로컬 환경에서 사용)
     * @return 프론트엔드 origin
     */
    public String convertToFrontendOrigin(String origin, UserType requestedUserType) {
        if (origin == null || origin.trim().isEmpty()) {
            return ROLE_TO_LOCAL_DOMAIN.get(UserType.AUDIENCE);
        }

        // API 서브도메인 제거
        if (origin.contains(PROD_API_ORGANIZER_HOST)) {
            return origin.replace(PROD_API_ORGANIZER_HOST, PROD_ORGANIZER_HOST);
        }
        if (origin.contains(PROD_API_HOST)) {
            return origin.replace(PROD_API_HOST, PROD_AUDIENCE_HOST);
        }

        // 로컬 백엔드 포트를 프론트엔드 포트로 변환
        if (origin.contains(LOCAL_BACKEND_HOST)) {
            return requestedUserType == UserType.ORGANIZER
                    ? LOCAL_ORGANIZER_URL
                    : LOCAL_AUDIENCE_URL;
        }

        return origin;
    }

    /**
     * Origin에서 host 부분 추출 (포트 포함)
     */
    private String extractHost(String origin) {
        if (origin == null) {
            return null;
        }
        try {
            URI uri = new URI(origin);
            String host = uri.getHost();
            int port = uri.getPort();

            if (port != -1 && port != 80 && port != 443) {
                return host + ":" + port;
            }
            return host;
        } catch (Exception e) {
            log.warn("Failed to parse origin: {}", origin, e);
            // fallback: 간단한 파싱 시도
            return origin.replaceFirst("https?://", "").split("/")[0];
        }
    }
}
