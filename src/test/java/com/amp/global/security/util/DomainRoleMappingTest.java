package com.amp.global.security.util;

import com.amp.domain.user.entity.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static com.amp.global.security.util.DomainConstants.*;

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
            assertThat(domainRoleMapping.getUserTypeFromOrigin("LOCAL_AUDIENCE_URL"))
                    .isEqualTo(UserType.AUDIENCE);
        }

        @Test
        @DisplayName("localhost:5174 -> ORGANIZER")
        void localhost5174_returnsOrganizer() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin("LOCAL_ORGANIZER_URL"))
                    .isEqualTo(UserType.ORGANIZER);
        }

        @Test
        @DisplayName("ampnotice.kr -> AUDIENCE")
        void ampnoticeKr_returnsAudience() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin("PROD_AUDIENCE_URL"))
                    .isEqualTo(UserType.AUDIENCE);
        }

        @Test
        @DisplayName("wwwPROD_COOKIE_DOMAIN -> AUDIENCE")
        void wwwAmpnoticeKr_returnsAudience() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin("https://" + PROD_AUDIENCE_WWW_HOST))
                    .isEqualTo(UserType.AUDIENCE);
        }

        @Test
        @DisplayName("hostPROD_COOKIE_DOMAIN -> ORGANIZER")
        void hostAmpnoticeKr_returnsOrganizer() {
            assertThat(domainRoleMapping.getUserTypeFromOrigin("PROD_ORGANIZER_URL"))
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
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, "LOCAL_AUDIENCE_URL"))
                    .isTrue();
        }

        @Test
        @DisplayName("AUDIENCE + localhost:5174 -> 무효")
        void audience_localhost5174_invalid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, "LOCAL_ORGANIZER_URL"))
                    .isFalse();
        }

        @Test
        @DisplayName("ORGANIZER + localhost:5174 -> 유효")
        void organizer_localhost5174_valid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.ORGANIZER, "LOCAL_ORGANIZER_URL"))
                    .isTrue();
        }

        @Test
        @DisplayName("ORGANIZER + localhost:5173 -> 무효")
        void organizer_localhost5173_invalid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.ORGANIZER, "LOCAL_AUDIENCE_URL"))
                    .isFalse();
        }

        @Test
        @DisplayName("AUDIENCE + ampnotice.kr -> 유효")
        void audience_ampnoticeKr_valid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.AUDIENCE, "PROD_AUDIENCE_URL"))
                    .isTrue();
        }

        @Test
        @DisplayName("ORGANIZER + hostPROD_COOKIE_DOMAIN -> 유효")
        void organizer_hostAmpnoticeKr_valid() {
            assertThat(domainRoleMapping.isValidDomainForRole(UserType.ORGANIZER, "PROD_ORGANIZER_URL"))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("getCorrectDomain - 올바른 도메인 반환")
    class GetCorrectDomainTest {

        @Test
        @DisplayName("AUDIENCE + 로컬 환경 -> LOCAL_AUDIENCE_URL")
        void audience_local_returnsLocalhost5173() {
            assertThat(domainRoleMapping.getCorrectDomain(UserType.AUDIENCE, "LOCAL_ORGANIZER_URL"))
                    .isEqualTo("LOCAL_AUDIENCE_URL");
        }

        @Test
        @DisplayName("ORGANIZER + 로컬 환경 -> LOCAL_ORGANIZER_URL")
        void organizer_local_returnsLocalhost5174() {
            assertThat(domainRoleMapping.getCorrectDomain(UserType.ORGANIZER, "LOCAL_AUDIENCE_URL"))
                    .isEqualTo("LOCAL_ORGANIZER_URL");
        }

        @Test
        @DisplayName("AUDIENCE + 프로덕션 환경 -> PROD_AUDIENCE_URL")
        void audience_production_returnsAmpnoticeKr() {
            assertThat(domainRoleMapping.getCorrectDomain(UserType.AUDIENCE, "PROD_ORGANIZER_URL"))
                    .isEqualTo("PROD_AUDIENCE_URL");
        }

        @Test
        @DisplayName("ORGANIZER + 프로덕션 환경 -> PROD_ORGANIZER_URL")
        void organizer_production_returnsHostAmpnoticeKr() {
            assertThat(domainRoleMapping.getCorrectDomain(UserType.ORGANIZER, "PROD_AUDIENCE_URL"))
                    .isEqualTo("PROD_ORGANIZER_URL");
        }
    }

    @Nested
    @DisplayName("getCookieDomain - 쿠키 도메인 설정")
    class GetCookieDomainTest {

        @Test
        @DisplayName("로컬 환경 -> null (도메인 설정 안함)")
        void local_returnsNull() {
            assertThat(domainRoleMapping.getCookieDomain("LOCAL_AUDIENCE_URL"))
                    .isNull();
        }

        @Test
        @DisplayName("프로덕션 환경 -> PROD_COOKIE_DOMAIN")
        void production_returnsAmpnoticeKr() {
            assertThat(domainRoleMapping.getCookieDomain("PROD_AUDIENCE_URL"))
                    .isEqualTo("PROD_COOKIE_DOMAIN");
        }

        @Test
        @DisplayName("프로덕션 host 환경 -> PROD_COOKIE_DOMAIN")
        void productionHost_returnsAmpnoticeKr() {
            assertThat(domainRoleMapping.getCookieDomain("PROD_ORGANIZER_URL"))
                    .isEqualTo("PROD_COOKIE_DOMAIN");
        }
    }

    @Nested
    @DisplayName("shouldCookieBeSecure - Secure 플래그 설정")
    class ShouldCookieBeSecureTest {

        @Test
        @DisplayName("로컬 환경 -> false")
        void local_returnsFalse() {
            assertThat(domainRoleMapping.shouldCookieBeSecure("LOCAL_AUDIENCE_URL"))
                    .isFalse();
        }

        @Test
        @DisplayName("프로덕션 환경 -> true")
        void production_returnsTrue() {
            assertThat(domainRoleMapping.shouldCookieBeSecure("PROD_AUDIENCE_URL"))
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("isProductionOrigin - 프로덕션 환경 판별")
    class IsProductionOriginTest {

        @Test
        @DisplayName("localhost -> false")
        void localhost_returnsFalse() {
            assertThat(domainRoleMapping.isProductionOrigin("LOCAL_AUDIENCE_URL"))
                    .isFalse();
        }

        @Test
        @DisplayName("ampnotice.kr -> true")
        void ampnoticeKr_returnsTrue() {
            assertThat(domainRoleMapping.isProductionOrigin("PROD_AUDIENCE_URL"))
                    .isTrue();
        }

        @Test
        @DisplayName("hostPROD_COOKIE_DOMAIN -> true")
        void hostAmpnoticeKr_returnsTrue() {
            assertThat(domainRoleMapping.isProductionOrigin("PROD_ORGANIZER_URL"))
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