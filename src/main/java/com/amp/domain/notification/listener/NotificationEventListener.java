package com.amp.domain.notification.listener;

import com.amp.domain.notice.event.NoticeCreatedEvent;
import com.amp.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NoticeCreatedEvent event) {
        try {
            notificationService.sendNewNoticeNotification(event);
        } catch (Exception e) {
            log.error(
                    "[FCM] noticeId={} categoryId={} 알림 전송 실패",
                    event.getNoticeId(),
                    event.getCategoryId(),
                    e
            );
        }
    }
}
