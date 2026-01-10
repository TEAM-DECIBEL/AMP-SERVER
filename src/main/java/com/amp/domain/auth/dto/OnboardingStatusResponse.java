package com.amp.domain.auth.dto;

import com.amp.domain.user.entity.RegistrationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OnboardingStatusResponse {
    private RegistrationStatus status;
    private boolean isCompleted;
}