package com.amp.global.security;

import com.amp.domain.user.RegistrationStatus;
import com.amp.domain.user.User;
import com.amp.domain.user.repository.UserRepository;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class OnboardingCheckFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    private static final List<String> SKIP_PATHS = Arrays.asList(
            "/api/auth/onboarding",
            "/api/auth/login",
            "/oauth2",
            "/login",
            "/h2-console"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 체크 건너뛸 경로인지 확인
        if (shouldSkipOnboardingCheck(path)) {
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

        // 사용자 이메일 추출
        String email = authentication.getName();

        // 온보딩 상태 확인
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null && user.getRegistrationStatus() == RegistrationStatus.PENDING) {
            log.warn("Onboarding not completed for user: {}", email);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("""
                    {
                        "error": "OnboardingRequired",
                        "message": "온보딩을 완료해주세요.",
                        "onboardingUrl": "/api/auth/onboarding/complete"
                    }
                    """);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipOnboardingCheck(String path) {
        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }
}