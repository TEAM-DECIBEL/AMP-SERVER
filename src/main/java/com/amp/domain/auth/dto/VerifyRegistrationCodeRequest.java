package com.amp.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyRegistrationCodeRequest {

    @NotBlank(message = "가입코드는 필수입니다.")
    @Size(min = 4, max = 20, message = "가입코드는 4~20자 사이여야 합니다.")
    private String registrationCode;

    @NotBlank(message = "주최사명은 필수입니다.")
    @Size(min = 2, max = 12, message = "주최사명은 2~12자 사이여야 합니다.")
    private String organizerName;
}
