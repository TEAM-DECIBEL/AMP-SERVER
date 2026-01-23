package com.amp.domain.notification.dto.response;

public record NotificationResponse(
        Long notificationId,
        Long festivalId,
        String title,
        String message,
        boolean isRead,
        Long noticeId,
        String createdData
) {
}
