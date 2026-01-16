package com.amp.domain.festival.common.repository;

import com.amp.domain.festival.common.entity.FestivalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalScheduleRepository extends JpaRepository<FestivalSchedule, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE FestivalSchedule f SET f.deletedAt = CURRENT_TIMESTAMP WHERE f.festival.id = :festivalId AND f.deletedAt IS NULL")
    void softDeleteByFestivalId(@Param("festivalId") Long festivalId);

}
