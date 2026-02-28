package com.amp.global.security.util;

import com.amp.domain.user.entity.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainRoleMappingTest {

    private DomainRoleMapping domainRoleMapping;

    @BeforeEach
    void setUp() {
        domainRoleMapping = new DomainRoleMapping();
    }

    @Nested
    @DisplayName("getUserTypeFromOrigin - Origin에서 UserType 추출")
    class GetUserTypeFromOriginTest {

        @Test
        @DisplayName("localhost:5173 -> AUDIENCE")
        void localhost5173_returnsAudience() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin("http://localhost:5173"))
                    .isEqualTo(UserType.AUDIENCE);
        }

        @Test
        @DisplayName("localhost:5174 -> ORGANIZER")
        void localhost5174_returnsOrganizer() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin("http://localhost:5174"))
                    .isEqualTo(UserType.ORGANIZER);
        }

        @Test
        @DisplayName("ampnotice.kr -> AUDIENCE")
        void ampnoticeKr_returnsAudience() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin("https://ampnotice.kr"))
                    .isEqualTo(UserType.AUDIENCE);
        }

        @Test
        @DisplayName("www.ampnotice.kr -> AUDIENCE")
        void wwwAmpnoticeKr_returnsAudience() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin("https://www.ampnotice.kr"))
                    .isEqualTo(UserType.AUDIENCE);
        }

        @Test
        @DisplayName("host.ampnotice.kr -> ORGANIZER")
        void hostAmpnoticeKr_returnsOrganizer() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin("https://host.ampnotice.kr"))
                    .isEqualTo(UserType.ORGANIZER);
        }

        @Test
        @DisplayName("null -> AUDIENCE (기본값)")
        void nullOrigin_returnsAudience() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin(null))
                    .isEqualTo(UserType.AUDIENCE);
        }
    }

    @Nested
    @DisplayName("isValidDomainForRole - 도메인-역할 검증")
    class IsValidDomainForRoleTest {

        @Test
        @DisplayName("AUDIENCE + localhost:5173 -> 유효")
        void audience_localhost5173_valid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, "http://localhost:5173"))
                    .isTrue();
        }

        @Test
        @DisplayName("AUDIENCE + localhost:5174 -> 무효")
        void audience_localhost5174_invalid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, "http://localhost:5174"))
                    .isFalse();
        }

        @Test
        @DisplayName("ORGANIZER + localhost:5174 -> 유효")
        void organizer_localhost5174_valid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.ORGANIZER, "http://localhost:5174"))
                    .isTrue();
        }

        @Test
        @DisplayName("ORGANIZER + localhost:5173 -> 무효")
        void organizer_localhost5173_invalid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.ORGANIZER, "http://localhost:5173"))
                    .isFalse();
        }

        @Test
        @DisplayName("AUDIENCE + ampnotice.kr -> 유효")
        void audience_ampnoticeKr_valid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, "https://ampnotice.kr"))
                    .isTrue();
        }

        @Test
        @DisplayName("ORGANIZER + host.ampnotice.kr -> 유효")
        void organizer_hostAmpnoticeKr_valid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.ORGANIZER, "https://host.ampnotice.kr"))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("getCorrectDomain - 올바른 도메인 반환")
    class GetCorrectDomainTest {

        @Test
        @DisplayName("AUDIENCE + 로컬 환경 -> http://localhost:5173")
        void audience_local_returnsLocalhost5173() {
            assertThat(domainRoleMapping.getCorrectDomain(UserType.AUDIENCE, "http://localhost:5174"))
                    .isEqualTo("http://localhost:5173");
        }

        @Test
        @DisplayName("ORGANIZER + 로컬 환경 -> http://localhost:5174")
        void organizer_local_returnsLocalhost5174() {
            assertThat(domainRoleMapping.getCorrectDomain(UserType.ORGANIZER, "http://localhost:5173"))
                    .isEqualTo("http://localhost:5174");
        }

        @Test
        @DisplayName("AUDIENCE + 프로덕션 환경 -> https://ampnotice.kr")
        void audience_production_returnsAmpnoticeKr() {
            assertThat(domainRoleMapping.getCorrectDomain(UserType.AUDIENCE, "https://host.ampnotice.kr"))
                    .isEqualTo("https://ampnotice.kr");
        }

        @Test
        @DisplayName("ORGANIZER + 프로덕션 환경 -> https://host.ampnotice.kr")
        void organizer_production_returnsHostAmpnoticeKr() {
            assertThat(domainRoleMapping.getCorrectDomain(UserType.ORGANIZER, "https://ampnotice.kr"))
                    .isEqualTo("https://host.ampnotice.kr");
        }
    }

    @Nested
    @DisplayName("getCookieDomain - 쿠키 도메인 설정")
    class GetCookieDomainTest {

        @Test
        @DisplayName("로컬 환경 -> null (도메인 설정 안함)")
        void local_returnsNull() {
            assertThat(domainRoleMapping.getCookieDomain("http://localhost:5173"))
                    .isNull();
        }

        @Test
        @DisplayName("프로덕션 환경 -> .ampnotice.kr")
        void production_returnsAmpnoticeKr() {
            assertThat(domainRoleMapping.getCookieDomain("https://ampnotice.kr"))
                    .isEqualTo(".ampnotice.kr");
        }

        @Test
        @DisplayName("프로덕션 host 환경 -> .ampnotice.kr")
        void productionHost_returnsAmpnoticeKr() {
            assertThat(domainRoleMapping.getCookieDomain("https://host.ampnotice.kr"))
                    .isEqualTo(".ampnotice.kr");
        }
    }

    @Nested
    @DisplayName("shouldCookieBeSecure - Secure 플래그 설정")
    class ShouldCookieBeSecureTest {

        @Test
        @DisplayName("로컬 환경 -> false")
        void local_returnsFalse() {
            assertThat(domainRoleMapping.shouldCookieBeSecure("http://localhost:5173"))
                    .isFalse();
        }

        @Test
        @DisplayName("프로덕션 환경 -> true")
        void production_returnsTrue() {
            assertThat(domainRoleMapping.shouldCookieBeSecure("https://ampnotice.kr"))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("isProductionOrigin - 프로덕션 환경 판별")
    class IsProductionOriginTest {

        @Test
        @DisplayName("localhost -> false")
        void localhost_returnsFalse() {
            assertThat(domainRoleMapping.isProductionOrigin("http://localhost:5173"))
                    .isFalse();
        }

        @Test
        @DisplayName("ampnotice.kr -> true")
        void ampnoticeKr_returnsTrue() {
            assertThat(domainRoleMapping.isProductionOrigin("https://ampnotice.kr"))
                    .isTrue();
        }

        @Test
        @DisplayName("host.ampnotice.kr -> true")
        void hostAmpnoticeKr_returnsTrue() {
            assertThat(domainRoleMapping.isProductionOrigin("https://host.ampnotice.kr"))
                    .isTrue();
        }

        @Test
        @DisplayName("null -> false")
        void nullOrigin_returnsFalse() {
            assertThat(domainRoleMapping.isProductionOrigin(null))
                    .isFalse();
        }
    }
}
