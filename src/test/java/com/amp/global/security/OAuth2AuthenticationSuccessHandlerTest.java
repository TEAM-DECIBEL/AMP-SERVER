package com.amp.global.security;

import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.security.util.DomainRoleMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    private DomainRoleMapping domainRoleMapping;

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

        ReflectionTestUtils.setField(successHandler, "cookieName", "accessToken");
        ReflectionTestUtils.setField(successHandler, "cookieMaxAge", 3600);
        ReflectionTestUtils.setField(successHandler, "cookieSameSite", "None");
    }

    @Nested
    @DisplayName("리다이렉트 URL 테스트")
    class RedirectUrlTest {

        @Test
        @DisplayName("PENDING 유저 - /onboarding으로 리다이렉트")
        void pendingUser_redirectsToOnboarding() throws Exception {
            // Given
            String email = "new@example.com";
            String origin = "http://localhost:5173";
            User user = createUser(email, RegistrationStatus.PENDING, null);

            setupMocks(email, user, origin);

            request.setParameter("state", "abc|userType=AUDIENCE|origin=" + origin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            String redirectUrl = response.getRedirectedUrl();
            assertThat(redirectUrl).isEqualTo("http://localhost:5173/onboarding");
            assertThat(redirectUrl).doesNotContain("token=");
            assertThat(redirectUrl).doesNotContain("status=");
        }

        @Test
        @DisplayName("COMPLETED 유저 + 도메인 일치 - 메인 페이지로 리다이렉트")
        void completedUser_domainMatch_redirectsToMain() throws Exception {
            // Given
            String email = "existing@example.com";
            String origin = "http://localhost:5173";
            User user = createUser(email, RegistrationStatus.COMPLETED, UserType.AUDIENCE);

            setupMocks(email, user, origin);
            when(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, origin)).thenReturn(true);

            request.setParameter("state", "abc|userType=AUDIENCE|origin=" + origin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            String redirectUrl = response.getRedirectedUrl();
            assertThat(redirectUrl).isEqualTo("http://localhost:5173");
            assertThat(redirectUrl).doesNotContain("token=");
            assertThat(redirectUrl).doesNotContain("callback");
        }

        @Test
        @DisplayName("COMPLETED 유저 + 도메인 불일치 - 올바른 도메인으로 리다이렉트")
        void completedUser_domainMismatch_redirectsToCorrectDomain() throws Exception {
            // Given
            String email = "organizer@example.com";
            String wrongOrigin = "http://localhost:5173"; // AUDIENCE 도메인
            String correctDomain = "http://localhost:5174"; // ORGANIZER 도메인
            User user = createUser(email, RegistrationStatus.COMPLETED, UserType.ORGANIZER);

            setupMocks(email, user, wrongOrigin);
            when(domainRoleMapping.isValidDomainForRole(UserType.ORGANIZER, wrongOrigin)).thenReturn(false);
            when(domainRoleMapping.getCorrectDomain(UserType.ORGANIZER, wrongOrigin)).thenReturn(correctDomain);

            request.setParameter("state", "abc|userType=AUDIENCE|origin=" + wrongOrigin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            String redirectUrl = response.getRedirectedUrl();
            assertThat(redirectUrl).isEqualTo("http://localhost:5174");
        }

        @Test
        @DisplayName("프로덕션 - PENDING 유저 온보딩 리다이렉트")
        void production_pendingUser_redirectsToOnboarding() throws Exception {
            // Given
            String email = "new@example.com";
            String origin = "https://ampnotice.kr";
            User user = createUser(email, RegistrationStatus.PENDING, null);

            setupMocks(email, user, origin);

            request.setParameter("state", "abc|userType=AUDIENCE|origin=" + origin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            String redirectUrl = response.getRedirectedUrl();
            assertThat(redirectUrl).isEqualTo("https://ampnotice.kr/onboarding");
        }

        @Test
        @DisplayName("프로덕션 - COMPLETED 유저 메인 리다이렉트")
        void production_completedUser_redirectsToMain() throws Exception {
            // Given
            String email = "existing@example.com";
            String origin = "https://host.ampnotice.kr";
            User user = createUser(email, RegistrationStatus.COMPLETED, UserType.ORGANIZER);

            setupMocks(email, user, origin);
            when(domainRoleMapping.isValidDomainForRole(UserType.ORGANIZER, origin)).thenReturn(true);

            request.setParameter("state", "abc|userType=ORGANIZER|origin=" + origin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            String redirectUrl = response.getRedirectedUrl();
            assertThat(redirectUrl).isEqualTo("https://host.ampnotice.kr");
        }
    }

    @Nested
    @DisplayName("쿠키 설정 테스트")
    class CookieTest {

        @Test
        @DisplayName("로컬 환경 - SameSite=None, Secure=false")
        void localEnvironment_cookieSettings() throws Exception {
            // Given
            String email = "test@example.com";
            String origin = "http://localhost:5173";
            User user = createUser(email, RegistrationStatus.COMPLETED, UserType.AUDIENCE);

            setupMocks(email, user, origin);
            when(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, origin)).thenReturn(true);
            when(domainRoleMapping.shouldCookieBeSecure(origin)).thenReturn(false);
            when(domainRoleMapping.getCookieDomain(origin)).thenReturn(null);

            request.setParameter("state", "abc|userType=AUDIENCE|origin=" + origin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            String setCookie = response.getHeader("Set-Cookie");
            assertThat(setCookie).contains("accessToken=mockToken");
            assertThat(setCookie).contains("SameSite=None");
            assertThat(setCookie).contains("HttpOnly");
            assertThat(setCookie).doesNotContain("Secure"); // 로컬은 Secure=false
        }

        @Test
        @DisplayName("프로덕션 환경 - SameSite=None, Secure=true, Domain=.ampnotice.kr")
        void productionEnvironment_cookieSettings() throws Exception {
            // Given
            String email = "test@example.com";
            String origin = "https://ampnotice.kr";
            User user = createUser(email, RegistrationStatus.COMPLETED, UserType.AUDIENCE);

            setupMocks(email, user, origin);
            when(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, origin)).thenReturn(true);
            when(domainRoleMapping.shouldCookieBeSecure(origin)).thenReturn(true);
            when(domainRoleMapping.getCookieDomain(origin)).thenReturn(".ampnotice.kr");

            request.setParameter("state", "abc|userType=AUDIENCE|origin=" + origin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            String setCookie = response.getHeader("Set-Cookie");
            assertThat(setCookie).contains("accessToken=mockToken");
            assertThat(setCookie).contains("SameSite=None");
            assertThat(setCookie).contains("Secure");
            assertThat(setCookie).contains("Domain=.ampnotice.kr");
            assertThat(setCookie).contains("HttpOnly");
        }

        @Test
        @DisplayName("쿠키 Max-Age 설정 확인")
        void cookie_maxAge() throws Exception {
            // Given
            String email = "test@example.com";
            String origin = "http://localhost:5173";
            User user = createUser(email, RegistrationStatus.COMPLETED, UserType.AUDIENCE);

            setupMocks(email, user, origin);
            when(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, origin)).thenReturn(true);
            when(domainRoleMapping.shouldCookieBeSecure(origin)).thenReturn(false);
            when(domainRoleMapping.getCookieDomain(origin)).thenReturn(null);

            request.setParameter("state", "abc|userType=AUDIENCE|origin=" + origin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            String setCookie = response.getHeader("Set-Cookie");
            assertThat(setCookie).contains("Max-Age=3600");
        }
    }

    @Nested
    @DisplayName("UserType 저장 테스트")
    class UserTypeSaveTest {

        @Test
        @DisplayName("PENDING 유저 - userType 저장됨")
        void pendingUser_userTypeSaved() throws Exception {
            // Given
            String email = "new@example.com";
            String origin = "http://localhost:5174";
            User user = createUser(email, RegistrationStatus.PENDING, null);

            setupMocks(email, user, origin);

            request.setParameter("state", "abc|userType=ORGANIZER|origin=" + origin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            verify(userRepository).save(argThat(savedUser ->
                    savedUser.getUserType() == UserType.ORGANIZER
            ));
        }

        @Test
        @DisplayName("COMPLETED 유저 - userType 저장 안됨")
        void completedUser_userTypeNotSaved() throws Exception {
            // Given
            String email = "existing@example.com";
            String origin = "http://localhost:5173";
            User user = createUser(email, RegistrationStatus.COMPLETED, UserType.AUDIENCE);

            setupMocks(email, user, origin);
            when(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, origin)).thenReturn(true);

            request.setParameter("state", "abc|userType=AUDIENCE|origin=" + origin);

            // When
            successHandler.onAuthenticationSuccess(request, response, authentication);

            // Then
            verify(userRepository, never()).save(any());
        }
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

    private void setupMocks(String email, User user, String origin) {
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(email)).thenReturn("mockToken");
        doNothing().when(cookieAuthorizationRequestRepository)
                .removeAuthorizationRequestCookies(any(), any());

        // DomainRoleMapping 기본 설정
        lenient().when(domainRoleMapping.getCookieDomain(origin)).thenReturn(null);
        lenient().when(domainRoleMapping.shouldCookieBeSecure(origin)).thenReturn(false);
    }
}
