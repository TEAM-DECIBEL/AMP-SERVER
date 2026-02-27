package com.amp.global.security;

import com.amp.domain.auth.exception.AuthErrorCode;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.response.error.AuthErrorResponse;
import com.amp.global.security.util.DomainRoleMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 도메인-역할 검증 필터
 * <p>
 * 이미 로그인된 사용자가 다른 도메인에서 API를 호출할 때
 * 사용자의 역할과 접근 도메인이 일치하는지 검증합니다.
 * <p>
 * - 불일치 시 API 요청: 403 JSON 응답 + correctDomain 정보
 * - 불일치 시 웹 요청: 올바른 도메인으로 리다이렉트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DomainRoleValidationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final DomainRoleMapping domainRoleMapping;
    private final ObjectMapper objectMapper;

    private static final List<String> SKIP_PATHS = Arrays.asList(
            "/oauth2",
            "/login",
            "/api/auth",
            "/api/public",
            "/h2-console",
            "/swagger-ui",
            "/v3/api-docs",
            "/actuator",
            "/error",
            "/favicon.ico"
    );

    // 공통 API 경로 (역할 검증 제외)
    private static final List<String> COMMON_API_PATHS = Arrays.asList(
            "/api/v1/common"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 검증 건너뛸 경로
        if (shouldSkipValidation(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증되지 않았거나 익명 사용자면 패스
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 요청 origin 추출
        String origin = extractOrigin(request);

        if (origin == null || origin.isEmpty()) {
            // origin을 추출할 수 없으면 패스
            filterChain.doFilter(request, response);
            return;
        }

        UserType userType = user.getUserType();

        if (userType == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 도메인-역할 검증
        if (!domainRoleMapping.isValidDomainForRole(userType, origin)) {
            String correctDomain = domainRoleMapping.getCorrectDomain(userType, origin);

            log.warn("Domain-Role mismatch: user={}, userType={}, origin={}, correctDomain={}",
                    email, userType, origin, correctDomain);

            if (isApiRequest(request)) {
                // API 요청: 403 JSON 응답
                sendJsonErrorResponse(response, correctDomain, userType);
            } else {
                // 웹 요청: 리다이렉트
                response.sendRedirect(correctDomain);
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipValidation(String path) {
        // 기본 스킵 경로
        if (SKIP_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }

        // 공통 API 경로
        if (COMMON_API_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }

        return false;
    }

    private String extractOrigin(HttpServletRequest request) {
        // 1. Origin 헤더
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.trim().isEmpty()) {
            return origin;
        }

        // 2. Referer 헤더에서 origin 추출
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.trim().isEmpty()) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                return uri.getScheme() + "://" + uri.getAuthority();
            } catch (Exception e) {
                log.warn("Failed to parse Referer header: {}", referer);
            }
        }

        return null;
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getContentType();
        String xRequestedWith = request.getHeader("X-Requested-With");

        // API 요청 판별
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return true;
        }
        if (contentType != null && contentType.contains("application/json")) {
            return true;
        }
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            return true;
        }

        // URI 기반 판별
        String uri = request.getRequestURI();
        return uri.startsWith("/api/");
    }

    private void sendJsonErrorResponse(HttpServletResponse response, String correctDomain, UserType userType)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        AuthErrorResponse errorResponse = AuthErrorResponse.domainRoleMismatch(
                AuthErrorCode.DOMAIN_ROLE_MISMATCH,
                correctDomain,
                userType.name()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
