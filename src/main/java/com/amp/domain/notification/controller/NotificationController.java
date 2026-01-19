package com.amp.domain.notification.controller;

import com.amp.domain.notification.dto.FcmTopicSubscribeRequest;
import com.amp.domain.notification.service.CategorySubscribeService;
import com.amp.global.annotation.ApiErrorCodes;
import com.amp.global.swagger.SwaggerResponseDescription;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/notifications")
public class NotificationController {

    private final CategorySubscribeService categorySubscribeService;

    @Operation(summary = "카테고리 구독")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_SUBSCRIBE)
    @PostMapping("/{categoryId}/subscribe")
    public void subscribeCategory(
            @PathVariable Long categoryId,
            @RequestBody FcmTopicSubscribeRequest request
    ) throws FirebaseMessagingException {
        categorySubscribeService.subscribe(categoryId, request.fcmToken());
    }

    @Operation(summary = "카테고리 구독 취소")
    @ApiErrorCodes(SwaggerResponseDescription.FAIL_TO_UNSUBSCRIBE)
    @DeleteMapping("/{categoryId}/subscribe")
    public void unsubscribeCategory(
            @PathVariable Long categoryId,
            @RequestBody FcmTopicSubscribeRequest request
    ) throws FirebaseMessagingException {
        categorySubscribeService.unsubscribe(categoryId, request.fcmToken());
    }

}


