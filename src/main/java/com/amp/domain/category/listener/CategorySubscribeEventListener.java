package com.amp.domain.category.listener;

import com.amp.domain.notification.entity.CategorySubscribeEvent;
import com.amp.global.fcm.service.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategorySubscribeEventListener {

    private final FCMService fcmService;

    @Async("categorySubscribeExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CategorySubscribeEvent event) {
        try {
            if (event.subscribe()) {
                fcmService.subscribeCategory(event.categoryId(), event.fcmToken());
            } else {
                fcmService.unsubscribeCategory(event.categoryId(), event.fcmToken());
            }
        } catch (Exception e) {
            log.error("[FCM 처리 실패] categoryId={}, error={}", event.categoryId(), e.getMessage());
        }
    }
}
