package com.amp.global.config;

import com.amp.global.security.CustomOAuth2AuthorizationRequestResolver;
import com.amp.global.security.JwtAuthenticationFilter;
import com.amp.global.security.OAuth2AuthenticationSuccessHandler;
import com.amp.global.security.OnboardingCheckFilter;
import com.amp.global.security.handler.CustomAccessDeniedHandler;
import com.amp.global.security.handler.CustomAuthenticationEntryPoint;
import com.amp.global.security.service.CustomOAuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuthUserService customOAuthUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final OnboardingCheckFilter onboardingCheckFilter;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:5174}")
    private String allowedOrigins;

    @Value("${app.oauth2.failure-redirect-uri:http://localhost:5173/login}")
    private String failureRedirectUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(csrf -> csrf.disable())

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 관리 - STATELESS (JWT 사용)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // OAuth2 인증 후 세션 무효화 (JWT만 사용)
                        .sessionFixation().newSession()
                )

                // 보안 헤더 설정
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(contentType -> contentType.disable())
                )

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/notices/*/bookmark"
                        ).authenticated()   // 북마크 기능 비로그인 환경에서 비허용

                        .requestMatchers(
                                "/api/v1/notices/*",    // 게시글 상세 조회 비회원 환경에서 허용
                                "/api/v1/common/festivals/*/notices"  // 페스티벌 내 공지 목록 조회
                        ).permitAll()

                        // 공개 엔드포인트
                        .requestMatchers(
                                "/api/v1/users/festivals", // 전체 공연 목록 조회
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/auth/logout",
                                "/oauth2/**",
                                "/login/**",
                                "/error",
                                "/favicon.ico",
                                "/h2-console/**",
                                "/test-login.html",
                                "/*.html",
                                "/*.css",
                                "/*.js",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/actuator/**"
                        ).permitAll()

                        // 주최사 권한
                        .requestMatchers("/api/organizer/**").hasRole("ORGANIZER")
                        .requestMatchers("/api/auth/onboarding/**").authenticated()

                        // 관객만 접근 가능
                        .requestMatchers("/api/v1/festivals/my").hasRole("AUDIENCE")
                        .requestMatchers("/api/v1/festivals/*/wishList").hasRole("AUDIENCE")
                        .requestMatchers("/api/v1/users/me/**").hasRole("AUDIENCE")

                        // 관리자 권한
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // Custom Authorization Request Resolver 등록
                        .authorizationEndpoint(authorization ->
                                authorization
                                        .baseUri("/oauth2/authorization")
                                        .authorizationRequestResolver(
                                                new CustomOAuth2AuthorizationRequestResolver(
                                                        clientRegistrationRepository
                                                )
                                        )
                        )
                        .redirectionEndpoint(redirection ->
                                redirection.baseUri("/login/oauth2/code/*")
                        )
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuthUserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 login failed", exception);
                            String targetUrl = UriComponentsBuilder
                                    .fromUriString(failureRedirectUri)
                                    .queryParam("error", "oauth2_failed")
                                    .queryParam("message", "로그인에 실패했습니다.")
                                    .build().toUriString();
                            response.sendRedirect(targetUrl);
                        })
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(onboardingCheckFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList()
        );
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}