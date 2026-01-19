package com.amp.domain.stage.repository;

import com.amp.domain.stage.entity.UserCongestionReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UserCongestionReportRepository extends JpaRepository<UserCongestionReport, Long> {

    @Query("SELECT r FROM UserCongestionReport r " +
            "WHERE r.stage.id = :stageId " +
            "AND r.reportedAt >= :oneHourAgo " +
            "AND FUNCTION('DATE', r.reportedAt) = CURRENT_DATE " +
            "ORDER BY r.reportedAt DESC"
    )
    List<UserCongestionReport> findRecentReports(
            @Param("stageId") Long stageId,
            @Param("oneHourAgo") LocalDateTime oneHourAgo
    );
}
