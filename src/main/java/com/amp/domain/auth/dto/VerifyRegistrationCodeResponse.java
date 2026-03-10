package com.amp.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyRegistrationCodeResponse {

    private boolean verified;
    private String message;
    private Integer remainingAttempts;
    private String organizerName;

    public static VerifyRegistrationCodeResponse success(String organizerName) {
        return VerifyRegistrationCodeResponse.builder()
                .verified(true)
                .message("가입이 완료되었습니다.")
                .organizerName(organizerName)
                .build();
    }

    public static VerifyRegistrationCodeResponse failure(int remainingAttempts) {
        return VerifyRegistrationCodeResponse.builder()
                .verified(false)
                .message("가입코드가 올바르지 않습니다.")
                .remainingAttempts(remainingAttempts)
                .build();
    }
}
