package com.amp.domain.stage.scheduler;

import com.amp.domain.stage.entity.CongestionLevel;
import com.amp.domain.stage.entity.UserCongestionReport;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.stage.repository.UserCongestionReportRepository;
import com.amp.domain.stage.service.CongestionCalculateService;
import com.amp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CongestionProcessor {

    private final RedisTemplate<String, String> redisTemplate;
    private final CongestionCalculateService calculationService;
    private final StageRepository stageRepository;
    private final UserRepository userRepository;
    private final UserCongestionReportRepository userReportRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public void processSingleStage(Long stageId) {
        String currentKey = "stage:reports:" + stageId;
        String processingKey = "stage:reports:processing:" + stageId + ":" + System.currentTimeMillis();

        Boolean renamed;
        try {
            renamed = redisTemplate.renameIfAbsent(currentKey, processingKey);
        } catch (Exception e) {
            log.debug("Redis 키 없음: stageId={}", stageId);
            calculationService.calculateAndSave(stageId);
            return;
        }

        if (Boolean.FALSE.equals(renamed)) {
            log.warn("Rename 실패: stageId={}", stageId);
            calculationService.calculateAndSave(stageId);
            return;
        }

        List<String> data = redisTemplate.opsForList().range(processingKey, 0, -1);

        if (data == null || data.isEmpty()) {
            redisTemplate.delete(processingKey);
            calculationService.calculateAndSave(stageId);
            return;
        }

        List<UserCongestionReport> reports = parseReports(data, stageId);

        if (!reports.isEmpty()) {
            userReportRepository.saveAll(reports);
            log.info("DB 저장 완료: stageId={}, {}건", stageId, reports.size());
        }

        calculationService.calculateAndSave(stageId);
        redisTemplate.delete(processingKey);
    }

    private List<UserCongestionReport> parseReports(List<String> data, Long stageId) {
        List<UserCongestionReport> reports = new ArrayList<>();

        for (String reportData : data) {
            try {
                String[] parts = reportData.split("\\|");

                if (parts.length != 3) {
                    log.warn("잘못된 데이터 형식: {}", reportData);
                    continue;
                }

                Long userId = Long.parseLong(parts[0]);
                CongestionLevel level = CongestionLevel.valueOf(parts[1]);
                LocalDateTime reportedAt = LocalDateTime.parse(parts[2], FORMATTER);

                UserCongestionReport report = UserCongestionReport.builder()
                        .user(userRepository.getReferenceById(userId))
                        .stage(stageRepository.getReferenceById(stageId))
                        .reportedLevel(level)
                        .reportedAt(reportedAt)
                        .build();

                reports.add(report);

            } catch (Exception e) {
                log.error("데이터 파싱 실패 (계속 진행): data={}", reportData, e);
            }
        }

        return reports;
    }
}