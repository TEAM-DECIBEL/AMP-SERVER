package com.amp.domain.notice.repository;

import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.entity.Bookmark;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByNoticeAndUser(Notice notice, User user);

    Optional<Bookmark> findByNoticeAndUser(Notice notice, User user);
}
