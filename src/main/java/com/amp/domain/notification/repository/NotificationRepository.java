package com.amp.domain.notification.repository;

import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notification.entity.Notification;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n JOIN FETCH n.notice no JOIN FETCH no.festival WHERE n.user = :user ORDER BY n.createdAt DESC")
    List<Notification> findByUserWithValidNoticeOrderByCreatedAtDesc(@Param("user") User user);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.notice = :notice")
    void deleteAllByNotice(@Param("notice") Notice notice);

}
