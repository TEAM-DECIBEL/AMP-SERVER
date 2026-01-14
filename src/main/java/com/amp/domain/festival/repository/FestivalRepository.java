package com.amp.domain.festival.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
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
            "AND f.deletedAt IS NULL")
    Page<Festival> findAllByMyUser(@Param("user") User user, Pageable pageable);
}
