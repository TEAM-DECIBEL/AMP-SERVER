package com.amp.domain.auth.dto;

import com.amp.domain.user.entity.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {

    @NotNull(message = "사용자 유형을 선택해주세요")
    private UserType userType;

    @NotBlank(message = "이름을 입력해주세요")
    private String name;
}
