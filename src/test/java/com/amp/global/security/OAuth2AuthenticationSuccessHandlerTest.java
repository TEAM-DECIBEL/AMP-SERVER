package com.amp.global.security;

import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private OAuth2AuthenticationSuccessHandler successHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        // 기본 설정값 주입
        ReflectionTestUtils.setField(successHandler, "cookieName", "accessToken");
        ReflectionTestUtils.setField(successHandler, "cookieMaxAge", 3600);
        ReflectionTestUtils.setField(successHandler, "cookieDomain", "localhost");
        ReflectionTestUtils.setField(successHandler, "cookieSecure", false);
        ReflectionTestUtils.setField(successHandler, "cookieSameSite", "Lax");
    }

    @Test
    @DisplayName("localhost:5173에서 AUDIENCE로 로그인 - PENDING 상태")
    void testAudienceLoginFromLocalhost5173Pending() throws Exception {
        // Given
        String email = "test@example.com";
        User user = createUser(email, RegistrationStatus.PENDING, null);

        setupMocks(email, user, "mockToken123");

        // Origin 헤더 설정 (localhost:5173 = AUDIENCE)
        request.addHeader("Origin", "http://localhost:5173");
        request.setParameter("state", "randomState|userType=AUDIENCE");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getUserType() == UserType.AUDIENCE &&
                        savedUser.getEmail().equals(email)
        ));

        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).contains("http://localhost:5173/callback");
        assertThat(redirectUrl).contains("token=mockToken123");
        assertThat(redirectUrl).contains("status=PENDING");
    }

    @Test
    @DisplayName("localhost:5174에서 ORGANIZER로 로그인 - PENDING 상태")
    void testOrganizerLoginFromLocalhost5174Pending() throws Exception {
        // Given
        String email = "organizer@example.com";
        User user = createUser(email, RegistrationStatus.PENDING, null);

        setupMocks(email, user, "mockToken456");

        // Origin 헤더 설정 (localhost:5174 = ORGANIZER)
        request.addHeader("Origin", "http://localhost:5174");
        request.setParameter("state", "randomState|userType=ORGANIZER");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getUserType() == UserType.ORGANIZER &&
                        savedUser.getEmail().equals(email)
        ));

        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).contains("http://localhost:5174/callback");
        assertThat(redirectUrl).contains("token=mockToken456");
        assertThat(redirectUrl).contains("status=PENDING");
    }

    @Test
    @DisplayName("ampnotice-host.kr에서 ORGANIZER로 로그인 - COMPLETED 상태")
    void testOrganizerLoginFromProductionDomainCompleted() throws Exception {
        // Given
        String email = "organizer@example.com";
        User user = createUser(email, RegistrationStatus.COMPLETED, UserType.ORGANIZER);

        setupMocks(email, user, "mockToken789");

        // Origin 헤더 설정
        request.addHeader("Origin", "https://www.ampnotice-host.kr");
        request.setParameter("state", "randomState|userType=ORGANIZER");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userRepository, never()).save(any()); // COMPLETED 상태라서 save 안 됨

        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).contains("https://www.ampnotice-host.kr/callback");
        assertThat(redirectUrl).contains("token=mockToken789");
        assertThat(redirectUrl).contains("status=COMPLETED");
    }

    @Test
    @DisplayName("ampnotice.kr에서 AUDIENCE로 로그인 - COMPLETED 상태")
    void testAudienceLoginFromProductionDomainCompleted() throws Exception {
        // Given
        String email = "audience@example.com";
        User user = createUser(email, RegistrationStatus.COMPLETED, UserType.AUDIENCE);

        setupMocks(email, user, "mockToken000");

        // Origin 헤더 설정
        request.addHeader("Origin", "https://www.ampnotice.kr");
        request.setParameter("state", "randomState|userType=AUDIENCE");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(userRepository, never()).save(any());

        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).contains("https://www.ampnotice.kr/callback");
        assertThat(redirectUrl).contains("token=mockToken000");
        assertThat(redirectUrl).contains("status=COMPLETED");
    }

    @Test
    @DisplayName("Referer 헤더로 Origin 추출 테스트")
    void testExtractCallbackFromReferer() throws Exception {
        // Given
        String email = "test@example.com";
        User user = createUser(email, RegistrationStatus.PENDING, null);

        setupMocks(email, user, "mockToken111");

        // Origin이 없고 Referer만 있는 경우
        request.addHeader("Referer", "http://localhost:5173/login");
        request.setParameter("state", "randomState|userType=AUDIENCE");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).contains("http://localhost:5173/callback");
    }

    @Test
    @DisplayName("www 도메인 처리 테스트 (www 제거)")
    void testWwwDomainHandling() throws Exception {
        // Given
        String email = "test@example.com";
        User user = createUser(email, RegistrationStatus.COMPLETED, UserType.ORGANIZER);

        setupMocks(email, user, "mockToken222");

        // www가 포함된 도메인
        request.addHeader("Origin", "https://www.ampnotice-host.kr");
        request.setParameter("state", "randomState|userType=ORGANIZER");

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        String redirectUrl = response.getRedirectedUrl();
        // www가 제거되고 ampnotice-host.kr로 리다이렉트
        assertThat(redirectUrl).contains("https://www.ampnotice-host.kr/callback");
    }

    // Helper methods
    private User createUser(String email, RegistrationStatus status, UserType userType) {
        User user = User.builder()
                .email(email)
                .registrationStatus(status)
                .userType(userType)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private void setupMocks(String email, User user, String token) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(email)).thenReturn(token);
        doNothing().when(cookieAuthorizationRequestRepository)
                .removeAuthorizationRequestCookies(any(), any());
    }
}