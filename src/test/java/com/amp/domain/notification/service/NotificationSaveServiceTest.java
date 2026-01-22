package com.amp.domain.notification.service;

import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notification.entity.Notification;
import com.amp.domain.notification.repository.NotificationRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class NotificationSaveServiceTest {

    @Autowired
    private NotificationSaveService notificationSaveService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Notification 저장 테스트")
    void saveNotificationTest() {
        // 1️⃣ 테스트용 User 생성 및 저장
        User user = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .build();
        userRepository.save(user);

        // 2️⃣ 테스트용 Notice 생성 및 저장
        Notice notice = Notice.builder()
                .title("테스트 공지")
                .content("공지 내용")
                .user(user)
                .build(); // id는 save() 후 DB에서 자동 생성됨

        // 실제 DB 저장 후 ID 확인 가능
        // noticeRepository.save(notice); // 만약 NoticeRepository 있으면 저장 필요

        // 3️⃣ Notification 생성
        Notification notification = Notification.builder()
                .user(user)
                .notice(notice)
                .title("알림 제목")
                .message("알림 내용")
                .build();

        // 4️⃣ Notification 저장
        notificationSaveService.save(notification);

        // 5️⃣ DB에 저장됐는지 검증
        Notification savedNotification = notificationRepository.findById(notification.getId())
                .orElseThrow(() -> new RuntimeException("Notification 저장 실패"));

        assertThat(savedNotification.getTitle()).isEqualTo("알림 제목");
        assertThat(savedNotification.getMessage()).isEqualTo("알림 내용");
        assertThat(savedNotification.getUser().getId()).isEqualTo(user.getId());
        assertThat(savedNotification.getNotice()).isEqualTo(notice);
    }
}
