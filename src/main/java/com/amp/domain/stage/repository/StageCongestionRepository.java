package com.amp.domain.stage.repository;

import com.amp.domain.stage.entity.StageCongestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StageCongestionRepository extends JpaRepository<StageCongestion, Long> {

    @Query(value = """
                SELECT sc.* FROM stage_congestion sc
                WHERE sc.congestion_id IN (
                SELECT MAX(congestion_id) 
                FROM stage_congestion 
                WHERE stage_id IN :stageIds 
                GROUP BY stage_id
                )
            """, nativeQuery = true)
    List<StageCongestion> findLatestByStageIds(@Param("stageIds") List<Long> stageIds);

    @Query("SELECT sc FROM StageCongestion sc " +
            "WHERE sc.stage.id = :stageId " +
            "ORDER BY sc.measuredAt DESC LIMIT 1")
    Optional<StageCongestion> findLatestByStageId(@Param("stageId") Long stageId);

}
