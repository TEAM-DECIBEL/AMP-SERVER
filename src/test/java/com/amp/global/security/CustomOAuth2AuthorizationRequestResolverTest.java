package com.amp.global.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2AuthorizationRequestResolverTest {

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    private CustomOAuth2AuthorizationRequestResolver resolver;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        resolver = new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
        request = new MockHttpServletRequest();

        // Mock ClientRegistration
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("test-client-id")
                .clientSecret("test-client-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .build();

        when(clientRegistrationRepository.findByRegistrationId("google"))
                .thenReturn(clientRegistration);
    }

    @Test
    @DisplayName("localhost:5173 Origin에서 AUDIENCE 자동 감지")
    void testAudienceDetectionFromLocalhost5173() {
        // Given
        request.setRequestURI("/oauth2/authorization/google");
        request.addHeader("Origin", "http://localhost:5173");

        // When
        OAuth2AuthorizationRequest authRequest = resolver.resolve(request, "google");

        // Then
        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getState()).contains("userType=AUDIENCE");
    }

    @Test
    @DisplayName("localhost:5174 Origin에서 ORGANIZER 자동 감지")
    void testOrganizerDetectionFromLocalhost5174() {
        // Given
        request.setRequestURI("/oauth2/authorization/google");
        request.addHeader("Origin", "http://localhost:5174");

        // When
        OAuth2AuthorizationRequest authRequest = resolver.resolve(request, "google");

        // Then
        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getState()).contains("userType=ORGANIZER");
    }

    @Test
    @DisplayName("ampnotice-host.kr 도메인에서 ORGANIZER 자동 감지")
    void testOrganizerDetectionFromHostDomain() {
        // Given
        request.setRequestURI("/oauth2/authorization/google");
        request.addHeader("Origin", "https://www.ampnotice-host.kr");

        // When
        OAuth2AuthorizationRequest authRequest = resolver.resolve(request, "google");

        // Then
        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getState()).contains("userType=ORGANIZER");
    }

    @Test
    @DisplayName("ampnotice.kr 도메인에서 AUDIENCE 자동 감지")
    void testAudienceDetectionFromMainDomain() {
        // Given
        request.setRequestURI("/oauth2/authorization/google");
        request.addHeader("Origin", "https://www.ampnotice.kr");

        // When
        OAuth2AuthorizationRequest authRequest = resolver.resolve(request, "google");

        // Then
        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getState()).contains("userType=AUDIENCE");
    }

    @Test
    @DisplayName("명시적 userType 파라미터가 있으면 우선 사용")
    void testExplicitUserTypeParameter() {
        // Given
        request.setRequestURI("/oauth2/authorization/google");
        request.addHeader("Origin", "http://localhost:5173"); // AUDIENCE 도메인
        request.setParameter("userType", "ORGANIZER"); // 하지만 명시적으로 ORGANIZER 요청

        // When
        OAuth2AuthorizationRequest authRequest = resolver.resolve(request, "google");

        // Then
        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getState()).contains("userType=ORGANIZER");
    }

    @Test
    @DisplayName("Referer 헤더로 UserType 감지")
    void testUserTypeDetectionFromReferer() {
        // Given
        request.setRequestURI("/oauth2/authorization/google");
        request.addHeader("Referer", "http://localhost:5174/login");

        // When
        OAuth2AuthorizationRequest authRequest = resolver.resolve(request, "google");

        // Then
        assertThat(authRequest).isNotNull();
        assertThat(authRequest.getState()).contains("userType=ORGANIZER");
    }
}