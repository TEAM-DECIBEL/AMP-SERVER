package com.amp.domain.notification.controller;

import com.amp.domain.notification.dto.response.NotificationListResponse;
import com.amp.domain.notification.service.NotificationService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "User API")
@RequestMapping("/api/v1/users/notifications")
public class NotificationGetController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 스테이션")
    public ResponseEntity<BaseResponse<NotificationListResponse>> myNotifications() {

        NotificationListResponse response = notificationService.getMyNotifications();
        return ResponseEntity
                .status(SuccessStatus.NOTIFICATION_GET_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.NOTIFICATION_GET_SUCCESS.getMsg(), response));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 여부 처리")
    public ResponseEntity<BaseResponse<Void>> read(@PathVariable Long notificationId) {

        notificationService.readNotification(notificationId);

        return ResponseEntity
                .status(SuccessStatus.NOTIFICATION_SET_READ_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.NOTIFICATION_SET_READ_SUCCESS.getMsg(), null));
    }
}
