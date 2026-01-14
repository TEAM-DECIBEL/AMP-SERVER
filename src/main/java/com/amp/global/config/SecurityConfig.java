package com.amp.global.config;

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

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Value("${app.oauth2.failure-redirect-uri:http://localhost:3000/login}")
    private String failureRedirectUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(csrf -> csrf.disable())

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 관리 - STATELESS (JWT 사용)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 보안 헤더 설정
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())  // Clickjacking 방지
                        .contentTypeOptions(contentType -> contentType.disable())
                )

                // 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/common/v1/notices/*/bookmark"
                        ).authenticated()   // 북마크 기능 비로그인 환경에서 비허용

                        .requestMatchers(
                                "/api/common/v1/notices/*"
                        ).permitAll()   // 게시글 상세 조회 비회원 환경에서 허용

                        // 공개 엔드포인트
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/oauth2/**",
                                "/login/**",
                                "/h2-console/**",
                                "/test-login.html",
                                "/*.html",
                                "/*.css",
                                "/*.js",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**" // 스웨거랑 api 독스는 배포전 반드시 따로 관리 해야함 제발 나에게 상기시켜줘
                        ).permitAll()

                        // 주최사 권한
                        .requestMatchers("/api/organizer/**").hasRole("ORGANIZER")
                        .requestMatchers("/api/auth/onboarding/**").authenticated() // 온보딩 api는 일단 소셜로그인 거친 이후로 접근 가능

                        // 관리자 권한
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                /*// 예외 처리
                .exceptionHandling(exception -> exception
                        // 인증 실패 (401)
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("Unauthorized request to: {}", request.getRequestURI());
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("""
                                {
                                    "error": "Unauthorized",
                                    "message": "인증이 필요합니다.",
                                    "path": "%s"
                                }
                                """.formatted(request.getRequestURI()));
                        })
                        // 권한 부족 (403)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("Access denied to: {}", request.getRequestURI());
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("""
                                {
                                    "error": "Forbidden",
                                    "message": "권한이 없습니다.",
                                    "path": "%s"
                                }
                                """.formatted(request.getRequestURI()));
                        }) 이렇게 하면 근데 결국 dispatcher servlet 이후 코드라 필터범위를 넘어가는거 같은데 테스트 해볼게요
                )*/

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // 엔드포인트 명시
                        .authorizationEndpoint(authorization ->
                                authorization.baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirection ->
                                redirection.baseUri("/login/oauth2/code/*")
                        )
                        // 사용자 정보 처리
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuthUserService)
                        )
                        // 성공 핸들러
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        // 실패 핸들러
                        .failureHandler((request, response, exception) -> {
                            log.error("OAuth2 login failed", exception, exception.getMessage());
                            String targetUrl = UriComponentsBuilder
                                    .fromUriString(failureRedirectUri)
                                    .queryParam("error", "oauth2_failed")
                                    .queryParam("message", "로그인에 실패 했습니다.") //todo 에러 모두 커스텀해서 넘길 수 있는지 확인
                                    .build().toUriString();
                            response.sendRedirect(targetUrl);
                        })
                )

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(onboardingCheckFilter, JwtAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 환경변수에서 읽어온 origins 설정
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
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