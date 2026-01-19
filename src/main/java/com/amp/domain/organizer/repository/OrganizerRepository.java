package com.amp.domain.organizer.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {

    Optional<Organizer> findByUserId(Long userId);

    boolean existsByFestivalAndUser(Festival festival, User user);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Organizer o SET o.deletedAt = CURRENT_TIMESTAMP WHERE o.festival.id = :festivalId AND o.deletedAt IS NULL")
    void softDeleteByFestivalId(@Param("festivalId") Long festivalId);

    @Query("SELECT COUNT(f) FROM Organizer o " +
            "JOIN Festival f ON o.id = f.id " +
            "WHERE o.id = :organizerId " +
            "AND f.status = :status " +
            "AND f.deletedAt IS NULL")
    Integer countFestivalsByOrganizerIdAndStatus(
            @Param("organizerId") Long organizerId,
            @Param("status") FestivalStatus status
    );
}
