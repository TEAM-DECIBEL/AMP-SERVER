package com.amp.domain.auth.dto;

import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OnboardingResponse {
    private String email;
    private UserType userType;
    private String name;
    private RegistrationStatus status;
    private String message;
}
