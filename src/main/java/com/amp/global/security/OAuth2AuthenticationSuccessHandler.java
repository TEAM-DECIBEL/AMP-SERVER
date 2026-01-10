package com.amp.global.security;

import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.repository.UserRepository;
import jakarta.servlet.ServletException;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Value("${app.oauth2.onboarding-uri:http://localhost:3000/onboarding}")
    private String onboardingUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        if (response.isCommitted()) {
            log.debug("Response has already been committed => 로그는 한번만 전송가능");
            return;
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        // 아마 카카오는 kakao_account.email 이라 나중에 확장하려면 provider 따라서 분기 처리해야함

        String token = jwtUtil.generateToken(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("OAuth 로그인 과정에서 유저 데이터가 저장되지 않았습니다."));

        String targetUrl;

        if (user.getRegistrationStatus() == RegistrationStatus.PENDING) {
            targetUrl = UriComponentsBuilder.fromUriString(onboardingUri)
                    .queryParam("token", token)
                    .queryParam("status", "pending")
                    .build().toUriString();
            log.info("Redirecting to onboarding page for user: {}", email);
        } else {
            targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", token)
                    .queryParam("status", "completed")
                    .build().toUriString();
            log.info("Redirecting to main page for user: {}", email);
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
