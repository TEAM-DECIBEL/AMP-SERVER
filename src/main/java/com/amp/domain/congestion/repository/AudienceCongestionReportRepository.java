package com.amp.domain.congestion.repository;

import com.amp.domain.congestion.entity.AudienceCongestionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AudienceCongestionReportRepository extends JpaRepository<AudienceCongestionReport, Long> {

    @Query("SELECT r FROM AudienceCongestionReport r " +
            "WHERE r.stage.id = :stageId " +
            "AND r.reportedAt >= :oneHourAgo " +
            "ORDER BY r.reportedAt DESC"
    )
    List<AudienceCongestionReport> findRecentReports(
            @Param("stageId") Long stageId,
            @Param("oneHourAgo") LocalDateTime oneHourAgo
    );
}
