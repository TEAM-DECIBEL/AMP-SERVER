package com.amp.domain.notification.dto.response;

import java.util.List;

public record NotificationListResponse(
        List<NotificationResponse> notificationResponseList
) {
}
