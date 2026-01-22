package com.amp.domain.notification.repository;

import com.amp.domain.notification.entity.Notification;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

//    long countByUserAndIsReadFalse(User user);
}
