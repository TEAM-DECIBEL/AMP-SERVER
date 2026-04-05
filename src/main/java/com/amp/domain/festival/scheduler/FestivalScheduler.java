package com.amp.domain.festival.scheduler;

import com.amp.domain.congestion.entity.Stage;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.congestion.service.StageService;
import com.amp.domain.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FestivalScheduler {

    private final FestivalRepository festivalRepository;
    private final StageService stageService;

    @Scheduled(cron = "1 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void updateFestivalStatus() {

        List<Festival> activeFestivals = festivalRepository.findAllByStatusNot(FestivalStatus.COMPLETED);

        activeFestivals.forEach(festival -> {
            try {
                FestivalStatus oldStatus = festival.getStatus();
                festival.updateStatus();
                FestivalStatus newStatus = festival.getStatus();

                if (oldStatus != newStatus) {
                    log.info("공연 [ID: {}] 상태 변경: {} -> {}", festival.getId(), oldStatus, newStatus);

                    if (newStatus == FestivalStatus.COMPLETED) {
                        List<Long> stageIds = festival.getStages().stream()
                                .map(Stage::getId)
                                .collect(Collectors.toList());
                        stageService.clearCongestionData(stageIds);
                        log.info("공연 [ID: {}] 혼잡도 데이터 삭제 완료", festival.getId());
                    }
                }
            } catch (Exception e) {
                log.error("공연 [ID: {}] 상태 업데이트 실패", festival.getId(), e);
                throw new IllegalStateException("혼잡도 데이터 정리 실패: " + festival.getId(), e);
            }
        });
    }
}
