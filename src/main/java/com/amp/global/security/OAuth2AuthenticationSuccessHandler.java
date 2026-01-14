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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Value("${app.oauth2.onboarding-uri}")
    private String onboardingUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        log.info("OAuth2 login success for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found after OAuth2 login"));

        // 요청 파라미터에서 userType 확인 (프론트엔드에서 전달)
        String userTypeParam = request.getParameter("userType");

        // Referer 헤더에서 경로 확인 (백업 방법) Referer URL 보고 Type추론 가능해서 OAuth에서 userType을 잘 안넘겨 줄수도 있어서 일단 백업용
        String referer = request.getHeader("Referer");

        UserType determinedUserType = determineUserType(userTypeParam, referer);

        log.info("Determined user type: {} for user: {}", determinedUserType, email);

        String targetUrl;

        // 온보딩이 필요한 경우
        if (user.getRegistrationStatus() == RegistrationStatus.PENDING) {
            // UserType 임시 설정
            user.updateUserType(determinedUserType);
            userRepository.save(user);

            // JWT 발급 (온보딩 진행을 위해)
            String token = jwtUtil.generateToken(email);

            targetUrl = UriComponentsBuilder.fromUriString(onboardingUri)
                    .queryParam("token", token)
                    .queryParam("userType", determinedUserType.name())
                    .queryParam("status", "pending")
                    .build().toUriString();

            log.info("Redirecting to onboarding: {}", targetUrl);
        }
        // 이미 온보딩 완료된 경우
        else {
            String token = jwtUtil.generateToken(email);

            targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", token)
                    .queryParam("status", "completed")
                    .build().toUriString();

            log.info("Redirecting to main app: {}", targetUrl);
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private UserType determineUserType(String userTypeParam, String referer) {
        // 1. 명시적 파라미터가 있으면 우선 사용
        if (userTypeParam != null) {
            try {
                return UserType.valueOf(userTypeParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid userType parameter: {}", userTypeParam); //todo 커스텀 예외처리로 돌리는게 맞나?
            }
        }

        // 2. Referer 헤더 분석
        if (referer != null) {
            if (referer.contains("/organizer/") || referer.contains("/organizer-login")) {
                return UserType.ORGANIZER;
            }
            if (referer.contains("/audience/") || referer.contains("/audience-login") || referer.contains("/login")) {
                return UserType.AUDIENCE;
            }
        }

        // 3. 기본값
        log.info("Using default user type: AUDIENCE");
        return UserType.AUDIENCE;
    }

}