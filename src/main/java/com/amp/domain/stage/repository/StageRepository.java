package com.amp.domain.stage.repository;

import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.stage.entity.Stage;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StageRepository extends JpaRepository<Stage, Long> {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Stage s SET s.deletedAt = CURRENT_TIMESTAMP WHERE s.festival.id = :festivalId AND s.deletedAt IS NULL")
    void softDeleteByFestivalId(@Param("festivalId") Long festivalId);

    @Query("SELECT s FROM Stage s WHERE s.festival.id = :festivalId AND s.deletedAt IS NULL ORDER BY s.id")
    Page<Stage> findByFestivalId(@Param("festivalId") Long festivalId, Pageable pageable);

    @Query("SELECT s.id FROM Stage s " +
            "JOIN s.festival f " +
            "WHERE s.deletedAt IS NULL " +
            "AND f.status IN :statuses")
    List<Long> findAllActiveIds(@Param("statuses") List<FestivalStatus> statuses);
}
