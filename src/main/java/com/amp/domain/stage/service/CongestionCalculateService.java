package com.amp.domain.stage.service;

import com.amp.domain.festival.entity.FestivalSchedule;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.stage.entity.CongestionLevel;
import com.amp.domain.stage.entity.Stage;
import com.amp.domain.stage.entity.StageCongestion;
import com.amp.domain.stage.entity.UserCongestionReport;
import com.amp.domain.stage.exception.StageErrorCode;
import com.amp.domain.stage.repository.StageCongestionRepository;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.stage.repository.UserCongestionReportRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CongestionCalculateService {

    private final UserCongestionReportRepository userCongestionReportRepository;
    private final StageCongestionRepository stageCongestionRepository;
    private final StageRepository stageRepository;
    private final FestivalScheduleRepository festivalScheduleRepository;

    @Transactional
    public void calculateAndSave(Long stageId) {
        Stage stage = stageRepository.findById(stageId).orElseThrow(
                () -> new CustomException(StageErrorCode.STAGE_NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        Optional<FestivalSchedule> todaySchedule = festivalScheduleRepository
                .findByFestivalIdAndFestivalDate(stage.getFestival().getId(), now.toLocalDate());

        if (todaySchedule.isEmpty()) {
            // 오늘 스케줄이 없으면 내일 스케줄이 8시간 이내인지 확인
            Optional<FestivalSchedule> tomorrowSchedule = festivalScheduleRepository
                    .findByFestivalIdAndFestivalDate(stage.getFestival().getId(), today.plusDays(1));

            if (tomorrowSchedule.isEmpty()) {
                return;
            }

            LocalDateTime tomorrowStart = today.plusDays(1).atTime(tomorrowSchedule.get().getFestivalTime());
            if (now.isBefore(tomorrowStart.minusHours(8))) {
                return;
            }
        }

        List<UserCongestionReport> reports = userCongestionReportRepository.findRecentReports(stageId, now.minusHours(1));

        if (reports.isEmpty()) {
            saveDefault(stage, now);
            return;
        }

        double totalWeight = 0.0;
        double weightedSum = 0.0;

        for (UserCongestionReport report : reports) {
            double weight = calculateWeight(report.getReportedAt(), now);
            weightedSum += (weight * report.getReportedLevel().getScore());
            totalWeight += weight;
        }

        double finalScore = weightedSum / totalWeight;

        stageCongestionRepository.save(StageCongestion.builder()
                .stage(stage)
                .congestionLevel(CongestionLevel.fromScore(finalScore))
                .currentCount(reports.size())
                .measuredAt(now)
                .build());
    }

    private double calculateWeight(LocalDateTime reportTime, LocalDateTime now) {
        long diff = ChronoUnit.MINUTES.between(reportTime, now);
        if (diff <= 15) return 1.0;
        if (diff <= 30) return 0.75;
        if (diff <= 45) return 0.5;
        return 0.25;
    }

    private void saveDefault(Stage stage, LocalDateTime now) {
        StageCongestion congestion = StageCongestion.builder()
                .stage(stage)
                .congestionLevel(CongestionLevel.NONE)
                .currentCount(0)
                .measuredAt(now)
                .build();

        stageCongestionRepository.save(congestion);
    }
}
