package com.amp.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FcmTopicSubscribeRequest(
        @NotBlank(message = "FCM 토큰은 필수입니다")
        String fcmToken
) {
}
