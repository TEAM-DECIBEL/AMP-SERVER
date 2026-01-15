package com.amp.domain.notice.repository;

import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.entity.Bookmark;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByNoticeAndUser(Notice notice, User user);

    Optional<Bookmark> findByNoticeAndUser(Notice notice, User user);

    @Query("SELECT b.notice.id FROM Bookmark b WHERE b.user = :user AND b.notice.id IN :noticeIds")
    List<Long> findNoticeIdsByUserAndNoticeIdIn(@Param("user") User user, @Param("noticeIds") List<Long> noticeIds);
}
