package com.amp.domain.notice.repository;

import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.entity.Bookmark;
import com.amp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    boolean existsByNoticeAndUser(Notice notice, User user);

    Optional<Bookmark> findByNoticeAndUser(Notice notice, User user);

    /**
     * 사용자가 저장한 공지사항 목록을 조회 (N+1 방지를 위한 fetch join)
     * Notice 엔티티의 @SQLRestriction으로 deleted_at IS NULL 자동 적용
     */
    @Query("""
        SELECT b FROM Bookmark b
        JOIN FETCH b.notice n
        JOIN FETCH n.festival f
        JOIN FETCH n.festivalCategory fc
        WHERE b.user.id = :userId
        ORDER BY b.createdAt DESC
        """)
    Page<Bookmark> findByUserIdWithDetails(
            @Param("userId") Long userId,
            Pageable pageable
    );

}
