package com.amp.domain.stage.scheduler;

import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.stage.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CongestionScheduler {

    private final CongestionProcessor congestionProcessor;
    private final StageRepository stageRepository;

    @Scheduled(cron = "0 0/15 * * * *")
    public void processCongestion() {
        List<FestivalStatus> targetStatuses = List.of(FestivalStatus.ONGOING, FestivalStatus.UPCOMING);
        List<Long> stageIds = stageRepository.findAllActiveIds(targetStatuses);

        for (Long id : stageIds) {
            try {
                congestionProcessor.processSingleStage(id);
            } catch (Exception e) {
                log.error("{} 처리 중 오류 발생: {}", id, e.getMessage());
            }
        }
    }
}
