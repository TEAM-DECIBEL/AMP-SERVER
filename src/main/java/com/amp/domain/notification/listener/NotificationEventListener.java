package com.amp.domain.notification.listener;

import com.amp.domain.notice.event.NoticeCreatedEvent;
import com.amp.domain.notification.service.NotificationService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    public void handle(NoticeCreatedEvent event) throws FirebaseMessagingException {
        notificationService.sendNewNoticeNotification(event);
    }
}
