package com.amp.domain.auth.dto;

import com.amp.domain.user.entity.UserType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {

    @NotNull(message = "사용자 타입을 선택해주세요.")
    private UserType userType;

    // 관객: 닉네임
    @Size(min = 2, max = 12, message = "닉네임은 2-12자 사이여야 합니다.")
    private String nickname;

    // 주최자 전용 필드
    @Size(min = 2, max = 12, message = "주최사명은 2-12자 사이여야 합니다.")
    private String organizerName;

    // 주최자 전용 - 연락처 정보 (선택사항)
    private String contactEmail;
    private String contactPhone;
    private String description;
}