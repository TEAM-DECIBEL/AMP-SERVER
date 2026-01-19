package com.amp.domain.stage.scheduler;

import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.stage.entity.CongestionLevel;
import com.amp.domain.stage.entity.UserCongestionReport;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.stage.repository.UserCongestionReportRepository;
import com.amp.domain.stage.service.CongestionCalculateService;
import com.amp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CongestionScheduler {
    private final RedisTemplate<String, String> redisTemplate;
    private final CongestionCalculateService calculationService;
    private final StageRepository stageRepository;
    private final UserRepository userRepository;
    private final UserCongestionReportRepository userReportRepository;

    @Scheduled(cron = "0 0/15 * * * *")
    public void processCongestion() {
        List<FestivalStatus> targetStatuses = List.of(FestivalStatus.ONGOING, FestivalStatus.UPCOMING);
        List<Long> stageIds = stageRepository.findAllActiveIds(targetStatuses);
        for (Long id : stageIds) {
            migrateRedisToDb(id);
            calculationService.calculateAndSave(id);
        }
    }

    private void migrateRedisToDb(Long stageId) {
        String key = "stage:reports:" + stageId;
        List<String> data = redisTemplate.opsForList().range(key, 0, -1);
        if (data == null || data.isEmpty()) return;

        List<UserCongestionReport> entities = data.stream().map(d -> {
            String[] p = d.split("\\|");
            return UserCongestionReport.builder()
                    .user(userRepository.getReferenceById(Long.parseLong(p[0])))
                    .stage(stageRepository.getReferenceById(stageId))
                    .reportedLevel(CongestionLevel.valueOf(p[1]))
                    .reportedAt(LocalDateTime.parse(p[2], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .build();
        }).toList();

        userReportRepository.saveAll(entities);
        redisTemplate.delete(key);
    }
}
