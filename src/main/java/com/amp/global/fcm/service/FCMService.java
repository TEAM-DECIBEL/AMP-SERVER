package com.amp.global.fcm.service;

import com.amp.global.fcm.exception.FCMErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final ObjectMapper objectMapper;

    @Value("${fcm.key.path}")
    private String SERVICE_ACCOUNT_JSON;
    @Value("${fcm.api.url}")
    private String FCM_API_URL;

    public void subscribeCategory(Long categoryId, String token) {
        try {
            FirebaseMessaging.getInstance()
                    .subscribeToTopic(List.of(token), topic(categoryId));
        } catch (FirebaseMessagingException e) {
            throw new IllegalArgumentException(FCMErrorCode.FAIL_TO_SEND_PUSH_ALARM.getMsg());
        }
    }

    public void unsubscribeCategory(Long categoryId, String token) {
        try {
            FirebaseMessaging.getInstance()
                    .unsubscribeFromTopic(List.of(token), topic(categoryId));
        } catch (FirebaseMessagingException e) {
            throw new IllegalArgumentException(FCMErrorCode.FAIL_TO_SEND_PUSH_ALARM.getMsg());
        }
    }

    public void sendCategoryNotice(Long categoryId, String title, String body) {
        try {
            Message message = Message.builder()
                    .setTopic(topic(categoryId))
                    .setNotification(
                            Notification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .build()
                    )
                    .build();

            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            throw new IllegalArgumentException(FCMErrorCode.FAIL_TO_SEND_PUSH_ALARM.getMsg());
        }
    }

    private String topic(Long categoryId) {
        return "category-" + categoryId;
    }

    public void sendCategoryTopicAlarm(Long categoryId, String title, String body) throws FirebaseMessagingException {
        String topic = "category-" + categoryId;
        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(
                        Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build()
                )
                .build();
        FirebaseMessaging.getInstance().send(message);
    }
}
