package com.amp.domain.category.listener;

import com.amp.domain.notification.entity.CategorySubscribeEvent;
import com.amp.global.fcm.service.FCMService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategorySubscribeEventListener {

    private final FCMService fcmService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(CategorySubscribeEvent event) {
        if (event.subscribe()) {
            fcmService.subscribeCategory(event.categoryId(), event.fcmToken());
        } else {
            fcmService.unsubscribeCategory(event.categoryId(), event.fcmToken());
        }
    }
}

