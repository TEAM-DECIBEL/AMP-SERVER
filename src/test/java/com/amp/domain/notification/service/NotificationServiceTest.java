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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private FCMService fcmService;
    @Mock private AlarmRepository alarmRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationSaveService notificationSaveService;

    // Notice / Festival / Alarm 을 @Mock으로 선언해야
    // notice.getId(), notice.getFestival().getId() 등 체이닝 호출 시 NPE 방지
    @Mock private Notice mockNotice;
    @Mock private Festival mockFestival;
    @Mock private Alarm mockAlarm;
    @Mock private User mockAlarmUser;

    @InjectMocks
    private NotificationService notificationService;

    private void givenNoticeReady() {
        given(mockNotice.getId()).willReturn(1L);
        given(mockNotice.getFestival()).willReturn(mockFestival);
        given(mockFestival.getId()).willReturn(1L);
    }

    private void givenAlarmReady() {
        given(alarmRepository.findAllByFestivalCategoryIdAndIsActiveTrue(1L))
                .willReturn(List.of(mockAlarm));
        given(mockAlarm.getUser()).willReturn(mockAlarmUser);
    }

    @Test
    @DisplayName("공지 생성 이벤트 발생 시 카테고리 토픽으로 FCM 알림을 전송한다")
    void sendNewNoticeNotification_success() throws FirebaseMessagingException {
        // given
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                1L, "공지사항", "축제명", mockNotice, "제목", LocalDateTime.now()
        );
        givenNoticeReady();
        givenAlarmReady();

        // when
        notificationService.sendNewNoticeNotification(event);

        // then
        verify(fcmService).sendCategoryTopicAlarm(
                eq(1L),                              // categoryId
                eq(1L),                              // noticeId
                eq(1L),                              // festivalId
                eq("공지사항 공지가 업로드 되었어요!"),  // title
                contains("[공지사항]"),               // body
                anyString()                          // timeData
        );
    }

    @Test
    @DisplayName("FCM 전송 실패 시 예외가 발생한다")
    void sendNewNoticeNotification_fail_whenFCMError() {
        // given
        NoticeCreatedEvent event = new NoticeCreatedEvent(
                1L, "공지사항", "축제명", mockNotice, "제목", LocalDateTime.now()
        );
        givenNoticeReady();
        givenAlarmReady();

        doThrow(new RuntimeException("FCM error"))
                .when(fcmService)
                .sendCategoryTopicAlarm(anyLong(), anyLong(), anyLong(), anyString(), anyString(), anyString());

        // when & then
        assertThrows(RuntimeException.class,
                () -> notificationService.sendNewNoticeNotification(event));
    }
}
