package com.amp.domain.notification.service;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.event.NoticeCreatedEvent;
import com.amp.domain.notification.entity.Alarm;
import com.amp.domain.notification.repository.AlarmRepository;
import com.amp.domain.notification.repository.NotificationRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.fcm.service.FCMService;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private FCMService fcmService;

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationSaveService notificationSaveService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("공지 생성 이벤트 발생 시 카테고리 토픽으로 FCM 알림을 전송한다")
    void sendNewNoticeNotification_success() throws FirebaseMessagingException {
        // given
        Festival mockFestival = mock(Festival.class);
        when(mockFestival.getId()).thenReturn(1L);

        Notice mockNotice = mock(Notice.class);
        when(mockNotice.getId()).thenReturn(1L);
        when(mockNotice.getFestival()).thenReturn(mockFestival);

        User mockAlarmUser = mock(User.class);
        Alarm mockAlarm = mock(Alarm.class);
        when(mockAlarm.getUser()).thenReturn(mockAlarmUser);

        when(alarmRepository.findAllByFestivalCategoryIdAndIsActiveTrue(1L))
                .thenReturn(List.of(mockAlarm));

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
                eq(1L),
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
        Festival mockFestival = mock(Festival.class);
        when(mockFestival.getId()).thenReturn(1L);

        Notice mockNotice = mock(Notice.class);
        when(mockNotice.getId()).thenReturn(1L);
        when(mockNotice.getFestival()).thenReturn(mockFestival);

        User mockAlarmUser = mock(User.class);
        Alarm mockAlarm = mock(Alarm.class);
        when(mockAlarm.getUser()).thenReturn(mockAlarmUser);

        when(alarmRepository.findAllByFestivalCategoryIdAndIsActiveTrue(1L))
                .thenReturn(List.of(mockAlarm));

        doThrow(new RuntimeException("FCM error"))
                .when(fcmService)
                .sendCategoryTopicAlarm(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyString());

        NoticeCreatedEvent event = new NoticeCreatedEvent(
                1L,
                "공지사항",
                "축제명",
                mockNotice,
                "제목",
                LocalDateTime.now()
        );

        // when & then
        assertThrows(RuntimeException.class,
                () -> notificationService.sendNewNoticeNotification(event));
    }
}
