package com.amp.domain.notification.controller;

import com.amp.domain.notification.dto.response.NotificationListResponse;
import com.amp.domain.notification.service.NotificationService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "User API")
@RequestMapping("/api/v1/users/notifications")
public class NotificationGetController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<BaseResponse<NotificationListResponse>> myNotifications() {

        NotificationListResponse response = notificationService.getMyNotifications();
        return ResponseEntity
                .status(SuccessStatus.NOTIFICATION_GET_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.NOTIFICATION_GET_SUCCESS.getMsg(), response));
    }
}
