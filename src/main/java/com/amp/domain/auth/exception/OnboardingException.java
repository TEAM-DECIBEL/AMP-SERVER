package com.amp.domain.auth.exception;

import com.amp.global.exception.CustomException;

public class OnboardingException extends CustomException {

    public OnboardingException(OnboardingErrorCode onboardingErrorCode) {
        super(onboardingErrorCode);
    }
}