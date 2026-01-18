package com.amp.domain.notification.service;

import com.amp.global.fcm.service.FCMService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategorySubscribeService {

    private final FCMService fcmService;

    public void subscribe(Long categoryId, String fcmToken) throws FirebaseMessagingException {
        fcmService.subscribeCategory(categoryId, fcmToken);
    }

    public void unsubscribe(Long categoryId, String fcmToken) throws FirebaseMessagingException {
        fcmService.unsubscribeCategory(categoryId, fcmToken);
    }
}
