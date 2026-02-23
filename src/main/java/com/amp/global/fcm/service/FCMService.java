package com.amp.global.fcm.service;

import com.amp.global.exception.CustomException;
import com.amp.global.fcm.exception.FCMErrorCode;
import com.amp.global.security.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
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
    private final AuthService authService;

    @Value("${fcm.key.path}")
    private String serviceAccountJson;

    public void subscribeCategory(Long categoryId, String token) {
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance()
                    .subscribeToTopic(List.of(token), topic(categoryId));

            log.info("{} tokens were subscribed successfully from topic {}",
                    response.getSuccessCount(), topic(categoryId));

            if (response.getFailureCount() > 0) {
                log.error("### [긴급] 구독 실패! FailureCount: {}", response.getFailureCount());
                response.getErrors().forEach(error -> {
                    log.error("### [긴급] 실패 상세 - index: {}, reason: {}",
                            error.getIndex(),
                            error.getReason());
                });

                log.error("### [긴급] 전체 에러 정보: {}", response.getErrors());
            }

        } catch (FirebaseMessagingException e) {
            log.error("FCM subscribe error: {}", e.getMessage());
            throw new CustomException(FCMErrorCode.FAIL_TO_SEND_PUSH_ALARM);
        }
    }

    public void unsubscribeCategory(Long categoryId, String token) {
        try {
            FirebaseMessaging.getInstance()
                    .unsubscribeFromTopic(List.of(token), topic(categoryId));
        } catch (FirebaseMessagingException e) {
            log.error("FCM unsubscribe error: {}", e.getMessage());
            throw new CustomException(FCMErrorCode.FAIL_TO_SEND_PUSH_ALARM);
        }
    }

    public void sendCategoryTopicAlarm(Long categoryId, Long noticeId, Long festivalId, String title, String noticeBody, String timeData) {
        String topic = topic(categoryId);
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(noticeBody)
                            .build())
                    .putData("title", title)
                    .putData("message", noticeBody)
                    .putData("time", timeData)
                    .putData("festivalId", String.valueOf(festivalId))
                    .putData("noticeId", String.valueOf(noticeId))
                    .build();
            FirebaseMessaging.getInstance().send(message);
            log.info("FCM 메시지 전송 성공: {}", topic);
        } catch (FirebaseMessagingException e) {
            log.error("FCM send error: {}", e.getMessage());
            throw new CustomException(FCMErrorCode.FAIL_TO_SEND_PUSH_ALARM);
        }
    }

    private String topic(Long festivalCategoryId) {
        return "festival-category-" + festivalCategoryId;
    }
}