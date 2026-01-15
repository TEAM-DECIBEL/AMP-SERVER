package com.amp.domain.user.repository;

import com.amp.domain.announcement.entity.UserSavedAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserSavedAnnouncementRepository extends JpaRepository<UserSavedAnnouncement, Long> {

    /**
     * 사용자가 저장한 공지사항 목록을 조회 (N+1 방지를 위한 fetch join)
     */
    @Query("""
        SELECT usa FROM UserSavedAnnouncement usa
        JOIN FETCH usa.announcement a
        JOIN FETCH a.festival f
        JOIN FETCH a.festivalCategory fc
        WHERE usa.user.id = :userId
        AND a.deletedAt IS NULL
        ORDER BY usa.createdAt DESC
        """)
    Page<UserSavedAnnouncement> findByUserIdWithDetails(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * 사용자가 특정 공지사항을 저장했는지 확인
     */
    boolean existsByUserUserIdAndAnnouncementAnnouncementId(Long userId, Long announcementId);

}
