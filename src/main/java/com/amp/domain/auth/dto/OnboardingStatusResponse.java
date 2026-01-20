package com.amp.domain.auth.dto;

import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OnboardingStatusResponse {
    private String email;
    private RegistrationStatus registrationStatus;
    private UserType userType;
    private boolean needsOnboarding;
}