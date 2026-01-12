package com.amp.domain.announcement.repository;

import com.amp.domain.announcement.entity.Announcement;
import com.amp.domain.announcement.entity.UserSavedAnnouncement;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSavedAnnouncementRepository extends JpaRepository<UserSavedAnnouncement, Long> {
    boolean existsByAnnouncementAndUser(Announcement announcement, User user);
}
