package com.amp.domain.notification.dto.response;

public record NotificationResponse(
        Long notificationId,
        String title,
        String message,
        boolean isRead,
        Long noticeId,
        String createdData
) {}
