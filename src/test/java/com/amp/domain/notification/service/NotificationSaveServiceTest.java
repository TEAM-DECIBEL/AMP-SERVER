package com.amp.domain.notification.service;

import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notification.entity.Notification;
import com.amp.domain.notification.repository.NotificationRepository;
import com.amp.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationSaveService 테스트")
class NotificationSaveServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationSaveService notificationSaveService;

    @Test
    @DisplayName("Notification 저장 테스트")
    void saveNotificationTest() {
        // given
        User mockUser = mock(User.class);
        Notice mockNotice = mock(Notice.class);

        Notification notification = Notification.builder()
                .user(mockUser)
                .notice(mockNotice)
                .title("알림 제목")
                .message("알림 내용")
                .build();

        // when
        notificationSaveService.save(notification);

        // then
        verify(notificationRepository, times(1)).save(notification);
    }
}
