package com.amp.domain.festival.common.repository;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.entity.FestivalStatus;
import com.amp.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {
    List<Festival> findAllByStatusNot(FestivalStatus festivalStatus);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Festival f SET f.deletedAt = CURRENT_TIMESTAMP WHERE f.id = :festivalId AND f.deletedAt IS NULL")
    void softDeleteById(@Param("festivalId") Long festivalId);

    @Query("SELECT f FROM Festival f " +
            "JOIN Organizer o ON o.festival = f " +
            "WHERE o.user = :user " +
            "AND f.deletedAt IS NULL " + "ORDER BY f.endDate DESC , f.startTime DESC , f.title ASC"
    )
    Page<Festival> findAllByMyUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT f FROM Festival f JOIN Organizer o ON o.festival = f " +
            "WHERE o.user = :user " +
            "AND f.status IN :statuses " + "AND f.deletedAt IS NULL" + " ORDER BY f.startDate ASC, f.startTime ASC, f.title ASC")
    Page<Festival> findActiveFestivalsByUser(@Param("user") User user, @Param("statuses") List<FestivalStatus> statuses, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Festival f JOIN Organizer o ON o.festival = f " +
            "WHERE o.user = :user AND f.status = :status AND f.deletedAt IS NULL")
    long countByOrganizerAndStatus(@Param("user") User user, @Param("status") FestivalStatus status);
}
