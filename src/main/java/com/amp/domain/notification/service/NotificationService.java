package com.amp.domain.notification.service;

import com.amp.domain.notice.event.NoticeCreatedEvent;
import com.amp.domain.notification.dto.response.NotificationListResponse;
import com.amp.domain.notification.dto.response.NotificationResponse;
import com.amp.domain.notification.entity.Alarm;
import com.amp.domain.notification.entity.Notification;
import com.amp.domain.notification.exception.NotificationErrorCode;
import com.amp.domain.notification.exception.NotificationException;
import com.amp.domain.notification.repository.AlarmRepository;
import com.amp.domain.notification.repository.NotificationRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.common.dto.TimeFormatter;
import com.amp.global.exception.CustomException;
import com.amp.global.fcm.service.FCMService;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FCMService fcmService;
    private final AlarmRepository alarmRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void sendNewNoticeNotification(NoticeCreatedEvent event) throws FirebaseMessagingException {

        List<Alarm> alarms =
                alarmRepository.findAllByFestivalCategoryIdAndIsActiveTrue(event.getCategoryId());

        String title = event.getCategoryName() + " 공지가 업로드 되었어요!";
        String noticeBody = "[" + event.getCategoryName() + "]" + event.getTitle();
        String timeData = TimeFormatter.formatTimeAgo(event.getCreatedAt());

        for (Alarm alarm : alarms) {
            Notification notification = Notification.builder()
                    .user(alarm.getUser())
                    .notice(event.getNotice())
                    .title(title)
                    .message(noticeBody)
                    .build();

            notificationRepository.save(notification);
        }
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

    @Transactional
    public void readNotification(Long notificationId) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));


        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().equals(user)) {
            throw new NotificationException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
        }

        notification.markAsRead();
    }

}

