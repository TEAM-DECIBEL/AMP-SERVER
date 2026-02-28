package com.amp.domain.festival.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.user.entity.Organizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FestivalRepository extends JpaRepository<Festival, Long> {

    List<Festival> findAllByStatusNot(FestivalStatus festivalStatus);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Festival f SET f.deletedAt = CURRENT_TIMESTAMP WHERE f.id = :festivalId AND f.deletedAt IS NULL")
    void softDeleteById(@Param("festivalId") Long festivalId);


    @Query("""
                SELECT f
                FROM Festival f
                    WHERE f.organizer = :organizer
                ORDER BY f.endDate DESC, f.startTime DESC, f.title ASC
            """)
    Page<Festival> findAllByMyUser(@Param("organizer") Organizer organizer, Pageable pageable);


    @Query("""
                SELECT f
                FROM Festival f
                WHERE f.organizer = :organizer
                  AND f.status IN :statuses
                ORDER BY f.startDate ASC, f.startTime ASC, f.title ASC
            """)
    Page<Festival> findActiveFestivalsByUser(@Param("organizer") Organizer organizer,
                                             @Param("statuses") List<FestivalStatus> statuses,
                                             Pageable pageable);


    @Query("""
                SELECT COUNT(f)
                FROM Festival f
                WHERE f.organizer = :organizer
                  AND f.status = :status
            """)
    long countByOrganizerAndStatus(@Param("organizer") Organizer organizer,
                                   @Param("status") FestivalStatus status);


    @Query("""
                SELECT f
                FROM Festival f
                WHERE f.endDate >= :today
                ORDER BY f.startDate ASC, f.startTime ASC, f.title ASC
            """)
    Page<Festival> findActiveFestivals(Pageable pageable, @Param("today") LocalDate today);


    @Query("""
                SELECT COUNT(f)
                FROM Festival f
                WHERE f.organizer.id = :userId
                  AND f.status = :status
            """)
    Long countFestivalsByUserIdAndStatus(@Param("userId") Long userId,
                                         @Param("status") FestivalStatus status);
}
