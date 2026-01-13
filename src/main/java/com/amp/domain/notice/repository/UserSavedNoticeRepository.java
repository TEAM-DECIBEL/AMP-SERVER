package com.amp.domain.notice.repository;

import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.entity.UserSavedNotice;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSavedNoticeRepository extends JpaRepository<UserSavedNotice, Long> {
    boolean existsByNoticeAndUser(Notice notice, User user);
}
