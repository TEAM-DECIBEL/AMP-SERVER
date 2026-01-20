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
public class OnboardingResponse {
    private Long userId;
    private String email;
    private String nickname;
    private UserType userType;
    private RegistrationStatus registrationStatus;
    private String message;
}