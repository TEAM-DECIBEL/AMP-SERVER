package com.amp.domain.stage.repository;

import com.amp.domain.stage.entity.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StageRepository extends JpaRepository<Stage, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Stage s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.festival.id = :festivalId AND s.deletedAt IS NULL")
    void softDeleteByFestivalId(@Param("festivalId") Long festivalId);
}
