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

    @Transactional
    public NotificationListResponse getMyNotifications() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        return new NotificationListResponse(notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getIsRead(),
                        n.getNotice().getId(),
                        TimeFormatter.formatTimeAgo(n.getCreatedAt())
                ))
                .toList());
    }

}

