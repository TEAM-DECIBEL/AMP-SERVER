package com.amp.global.security.util;

public final class DomainConstants {

    private DomainConstants() {

    }

    // ========== 로컬 환경 ==========
    public static final String LOCAL_AUDIENCE_HOST = "localhost:5173";
    public static final String LOCAL_ORGANIZER_HOST = "localhost:5174";
    public static final String LOCAL_BACKEND_HOST = "localhost:8080";

    public static final String LOCAL_AUDIENCE_URL = "http://" + LOCAL_AUDIENCE_HOST;
    public static final String LOCAL_ORGANIZER_URL = "http://" + LOCAL_ORGANIZER_HOST;
    public static final String LOCAL_BACKEND_URL = "http://" + LOCAL_BACKEND_HOST;

    // ========== 프로덕션 환경 ==========
    public static final String PROD_AUDIENCE_HOST = "ampnotice.kr";
    public static final String PROD_AUDIENCE_WWW_HOST = "www.ampnotice.kr";
    public static final String PROD_ORGANIZER_HOST = "host.ampnotice.kr";

    public static final String PROD_API_HOST = "api.ampnotice.kr";
    public static final String PROD_API_ORGANIZER_HOST = "api.host.ampnotice.kr";

    public static final String PROD_AUDIENCE_URL = "https://" + PROD_AUDIENCE_HOST;
    public static final String PROD_ORGANIZER_URL = "https://" + PROD_ORGANIZER_HOST;

    // ========== 쿠키 도메인 ==========
    public static final String PROD_COOKIE_DOMAIN = ".ampnotice.kr";

    // ========== 프론트엔드 경로 ==========
    public static final String ONBOARDING_PATH = "/onboarding";
}
