package com.amp.domain.notification.service;

import com.amp.domain.notice.event.NoticeCreatedEvent;
import com.amp.global.common.dto.TimeFormatter;
import com.amp.global.fcm.service.FCMService;
import com.google.firebase.messaging.FirebaseMessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FCMService fcmService;

    public void sendNewNoticeNotification(NoticeCreatedEvent event) throws FirebaseMessagingException {

        String title = event.getCategoryName()+" 공지가 업로드 되었어요!";
        String noticeBody = "["+event.getCategoryName()+"]" +event.getTitle();
        String timeData = TimeFormatter.formatTimeAgo(event.getCreatedAt());

        fcmService.sendCategoryTopicAlarm(
                event.getCategoryId(),
                title,
                noticeBody,
                timeData
        );
    }
}

