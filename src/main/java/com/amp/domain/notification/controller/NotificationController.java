package com.amp.domain.notification.controller;

import com.amp.domain.notification.dto.FcmTopicSubscribeRequest;
import com.amp.domain.notification.service.CategorySubscribeService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/notifications")
public class NotificationController {

    private final CategorySubscribeService categorySubscribeService;

    @PostMapping("/{categoryId}/subscribe")
    public void subscribeCategory(
            @PathVariable Long categoryId,
            @RequestBody FcmTopicSubscribeRequest request
    ) throws FirebaseMessagingException {
        categorySubscribeService.subscribe(categoryId, request.fcmToken());
    }
}


