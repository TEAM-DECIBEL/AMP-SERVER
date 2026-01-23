package com.amp.global.config;

import com.amp.global.security.*;
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
import java.util.List;

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
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,PATCH,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.oauth2.failure-redirect-uri:http://localhost:5173/login}")
    private String failureRedirectUri;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // ✅ CORS 설정 적용 (가장 먼저!)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ STATELESS 유지 (쿠키로 OAuth2 처리)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(contentType -> contentType.disable())
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/notices/*/bookmark"
                        ).authenticated()

                        .requestMatchers(
                                "/api/v1/notices/*",
                                "/api/v1/common/festivals/*/notices"
                        ).permitAll()

                        .requestMatchers(
                                "/",
                                "/api/v1/users/festivals",
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

                        .requestMatchers("/api/v1/organizer/**").hasRole("ORGANIZER")
                        .requestMatchers("/api/auth/onboarding/**").authenticated()
                        .requestMatchers("/api/v1/festivals/my").hasRole("AUDIENCE")
                        .requestMatchers("/api/v1/festivals/*/wishList").hasRole("AUDIENCE")
                        .requestMatchers("/api/v1/users/me/**").hasRole("AUDIENCE")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization ->
                                authorization
                                        .baseUri("/oauth2/authorization")
                                        .authorizationRequestResolver(
                                                new CustomOAuth2AuthorizationRequestResolver(
                                                        clientRegistrationRepository
                                                )
                                        )
                                        .authorizationRequestRepository(cookieAuthorizationRequestRepository)
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
                            log.error("Request URI: {}", request.getRequestURI());
                            log.error("Query String: {}", request.getQueryString());
                            log.error("Exception type: {}", exception.getClass().getName());

                            cookieAuthorizationRequestRepository
                                    .removeAuthorizationRequestCookies(request, response);

                            String targetUrl = UriComponentsBuilder
                                    .fromUriString(failureRedirectUri)
                                    .queryParam("error", "oauth2_failed")
                                    .queryParam("message", exception.getMessage())
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

        // Origins 파싱
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        log.info("✅ CORS Allowed Origins: {}", origins);

        configuration.setAllowedOrigins(origins);

        // Methods 파싱
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // Headers 파싱
        if ("*".equals(allowedHeaders.trim())) {
            configuration.setAllowedHeaders(Arrays.asList("*"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L);

        // ✅ 프론트엔드에서 접근 가능한 헤더 노출
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Set-Cookie",
                "Access-Control-Allow-Origin"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("✅ CORS Configuration initialized successfully");

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}