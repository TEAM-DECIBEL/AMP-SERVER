package com.amp.domain.stage.service;

import com.amp.domain.festival.entity.FestivalSchedule;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.stage.entity.CongestionLevel;
import com.amp.domain.stage.entity.Stage;
import com.amp.domain.stage.exception.StageErrorCode;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.user.entity.User;
import com.amp.global.exception.CustomException;
import com.amp.global.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CongestionReportService {

    private final StageRepository stageRepository;
    private final FestivalScheduleRepository festivalScheduleRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AuthService authService;

    private static final String USER_REPORT_KEY = "user:report:";
    private static final String STAGE_REPORTS_KEY = "stage:reports:";

    public void reportCongestion(Long stageId, CongestionLevel level) {

        if (level == null || level == CongestionLevel.NONE) {
            throw new CustomException(StageErrorCode.INVALID_CONGESTION_LEVEL);
        }

        User user = authService.getCurrentUser();
        Stage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new CustomException(StageErrorCode.STAGE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        validateReportTime(stage, now);

        String userReportKey = USER_REPORT_KEY + user.getId() + ":" + stageId;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(userReportKey))) {
            throw new CustomException(StageErrorCode.ALREADY_REPORTED_RECENTLY);
        }

        String stageReportKey = STAGE_REPORTS_KEY + stageId;
        String reportData = String.format("%s|%s|%s",
                user.getId(),
                level.name(),
                now.format(FORMATTER));

        redisTemplate.opsForList().rightPush(stageReportKey, reportData);
        redisTemplate.expire(stageReportKey, 1, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(userReportKey, "1", Duration.ofMinutes(15));

        log.info("혼잡도 입력 완료 (Redis 저장): userId={}, stageId={}, level={}",
                user.getId(), stageId, level);
    }

    private void validateReportTime(Stage stage, LocalDateTime now) {

        LocalDate today = now.toLocalDate();

        List<FestivalSchedule> schedules = festivalScheduleRepository
                .findByFestivalIdOrderByFestivalDate(stage.getFestival().getId());

        if (schedules.isEmpty()) {
            throw new CustomException(StageErrorCode.NO_SCHEDULE_FOUND);
        }

        FestivalSchedule todaySchedule = schedules.stream()
                .filter(s -> s.getFestivalDate().equals(today))
                .findFirst()
                .orElse(null);

        LocalDateTime enableInputTime = getEnableInputTime(todaySchedule, schedules, today);

        LocalDateTime enableEndTime = today.atTime(23, 59);

        if (now.isBefore(enableInputTime)) {
            log.warn("입력 불가 (너무 이름): stageId={}, now={}, enableFrom={}",
                    stage.getId(), now, enableInputTime);
            throw new CustomException(StageErrorCode.TOO_EARLY_TO_REPORT);
        }

        log.debug("입력 가능 시간 확인 완료: stageId={}, date={}, enableTime={} ~ {}",
                stage.getId(), today, enableInputTime.toLocalTime(), enableEndTime.toLocalTime());
    }

    private static LocalDateTime getEnableInputTime(FestivalSchedule todaySchedule, List<FestivalSchedule> schedules, LocalDate today) {
        if (todaySchedule == null) {

            LocalDate firstDate = schedules.getFirst().getFestivalDate();
            if (today.isBefore(firstDate)) {
                throw new CustomException(StageErrorCode.TOO_EARLY_TO_REPORT);
            }

            LocalDate lastDate = schedules.getLast().getFestivalDate();
            if (today.isAfter(lastDate)) {
                throw new CustomException(StageErrorCode.FESTIVAL_ENDED);
            }

            throw new CustomException(StageErrorCode.NO_SCHEDULE_TODAY);
        }

        LocalDateTime todayStart = today.atTime(todaySchedule.getFestivalTime());
        return todayStart.minusHours(8);
    }

}
