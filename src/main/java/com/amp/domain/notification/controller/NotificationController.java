package com.amp.domain.notification.controller;

import com.amp.domain.notification.dto.request.FcmTopicSubscribeRequest;
import com.amp.domain.notification.service.CategorySubscribeService;
import com.amp.domain.notification.service.NotificationService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import com.amp.global.swagger.SwaggerResponseDescription;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "User API")
@RequestMapping("/api/v1/festivals/{festivalId}/users/notifications")
public class NotificationController {

    private final CategorySubscribeService categorySubscribeService;
    private final NotificationService notificationService;

    @Operation(summary = "카테고리 구독")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_SUBSCRIBE)
    @PostMapping("/{categoryCode}/subscribe")
    public ResponseEntity<BaseResponse<Void>> subscribeCategory(
            @PathVariable Long festivalId,
            @PathVariable String categoryCode,
            @RequestBody FcmTopicSubscribeRequest request
    ) throws FirebaseMessagingException {
        categorySubscribeService.subscribe(festivalId, categoryCode, request.fcmToken());
        return ResponseEntity
                .status(SuccessStatus.SUBSCRIBE_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.SUBSCRIBE_SUCCESS.getMsg(), null));

    }

    @Operation(summary = "카테고리 구독 취소")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_UNSUBSCRIBE)
    @DeleteMapping("/{categoryCode}/subscribe")
    public ResponseEntity<BaseResponse<Void>> unsubscribeCategory(
            @PathVariable Long festivalId,
            @PathVariable String categoryCode,
            @RequestBody FcmTopicSubscribeRequest request
    ) throws FirebaseMessagingException {
        categorySubscribeService.unsubscribe(festivalId, categoryCode, request.fcmToken());
        return ResponseEntity
                .status(SuccessStatus.UNSUBSCRIBE_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.UNSUBSCRIBE_SUCCESS.getMsg(), null));
    }

}


