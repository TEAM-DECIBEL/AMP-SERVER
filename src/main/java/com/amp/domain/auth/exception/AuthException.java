package com.amp.domain.auth.exception;

import com.amp.global.exception.CustomException;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class AuthException extends CustomException {

    private final Map<String, Object> details;

    public AuthException(AuthErrorCode authErrorCode) {
        super(authErrorCode);
        this.details = new HashMap<>();
    }

    public AuthException(AuthErrorCode authErrorCode, Map<String, Object> details) {
        super(authErrorCode);
        this.details = details != null ? details : new HashMap<>();
    }

    public static AuthException domainRoleMismatch(String correctDomain, String userType) {
        Map<String, Object> details = new HashMap<>();
        details.put("correctDomain", correctDomain);
        details.put("userType", userType);
        return new AuthException(AuthErrorCode.DOMAIN_ROLE_MISMATCH, details);
    }

    public static AuthException onboardingRequired(String onboardingUrl) {
        Map<String, Object> details = new HashMap<>();
        details.put("onboardingUrl", onboardingUrl);
        return new AuthException(AuthErrorCode.ONBOARDING_REQUIRED, details);
    }
}
