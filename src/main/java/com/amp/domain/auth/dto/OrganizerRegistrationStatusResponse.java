package com.amp.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrganizerRegistrationStatusResponse {

    private boolean registered;
    private boolean verified;
    private boolean codeRequired;
    private String message;

    public static OrganizerRegistrationStatusResponse notRegistered() {
        return OrganizerRegistrationStatusResponse.builder()
                .registered(false)
                .verified(false)
                .codeRequired(false)
                .message("등록되지 않은 이메일입니다.")
                .build();
    }

    public static OrganizerRegistrationStatusResponse codeRequired() {
        return OrganizerRegistrationStatusResponse.builder()
                .registered(true)
                .verified(false)
                .codeRequired(true)
                .message("가입코드 검증이 필요합니다.")
                .build();
    }

    public static OrganizerRegistrationStatusResponse alreadyVerified() {
        return OrganizerRegistrationStatusResponse.builder()
                .registered(true)
                .verified(true)
                .codeRequired(false)
                .message("이미 검증이 완료되었습니다.")
                .build();
    }
}
