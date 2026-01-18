package com.amp.domain.festival.repository;

import com.amp.domain.festival.entity.FestivalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FestivalScheduleRepository extends JpaRepository<FestivalSchedule, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FestivalSchedule f SET f.deletedAt = CURRENT_TIMESTAMP WHERE f.festival.id = :festivalId AND f.deletedAt IS NULL")
    void softDeleteByFestivalId(@Param("festivalId") Long festivalId);

    @Query("SELECT fs FROM FestivalSchedule fs " +
            "WHERE fs.festival.id = :festivalId " +
            "ORDER BY fs.festivalDate")
    List<FestivalSchedule> findByFestivalIdOrderByFestivalDate(@Param("festivalId") Long festivalId);
}
