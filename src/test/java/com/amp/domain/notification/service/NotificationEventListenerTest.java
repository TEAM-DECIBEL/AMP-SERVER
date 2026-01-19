package com.amp.domain.notification.service;

import com.amp.domain.notice.event.NoticeCreatedEvent;
import com.amp.domain.notification.listener.NotificationEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @Test
    @DisplayName("공지 생성 이벤트 수신 시 NotificationService를 호출한다")
    void handle_noticeCreatedEvent_callNotificationService() throws Exception {        // given
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                1L,
                "공지사항",
                "민트 페스티벌",
                1L,
                "공지 제목",
                LocalDateTime.now()
        );

        // when
        listener.handle(event);

        // then
        verify(notificationService)
                .sendNewNoticeNotification(event);
    }
}
