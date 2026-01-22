package com.amp.domain.notification.service;

import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.event.NoticeCreatedEvent;
import com.amp.domain.user.entity.User;
import com.amp.global.fcm.service.FCMService;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private FCMService fcmService;

    @InjectMocks
    private NotificationService notificationService;
    // given
    User mockUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("테스트유저")
            .build();

    Notice mockNotice = Notice.builder()
            .title("공지 제목")
            .content("공지 내용")
            .user(mockUser)
            .build();

    @Test
    @DisplayName("공지 생성 이벤트 발생 시 카테고리 토픽으로 FCM 알림을 전송한다")
    void sendNewNoticeNotification_success() throws FirebaseMessagingException {
        // given
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                1L,
                "공지사항",
                "축제명",
                mockNotice,
                "제목",
                LocalDateTime.now()
        );

        // when
        notificationService.sendNewNoticeNotification(event);

        // then
        verify(fcmService).sendCategoryTopicAlarm(
                eq(1L),
                eq("공지사항 공지가 업로드 되었어요!"),
                contains("[공지사항]"),
                any()
        );
    }


    @Test
    @DisplayName("FCM 전송 실패 시 예외가 발생한다")
    void sendNewNoticeNotification_fail_whenFCMError() {
        // given
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                1L,
                "공지사항",
                "축제명",
                mockNotice,
                "제목",
                LocalDateTime.now()
        );

        doThrow(new RuntimeException("FCM error"))
                .when(fcmService)
                .sendCategoryTopicAlarm(anyLong(), anyString(), anyString(), anyString());

        // when & then
        assertThrows(RuntimeException.class,
                () -> notificationService.sendNewNoticeNotification(event));
    }
}
