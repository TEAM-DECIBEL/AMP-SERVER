package com.amp.domain.festival.common.scheduler;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.entity.FestivalStatus;
import com.amp.domain.festival.common.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FestivalScheduler {

    private final FestivalRepository festivalRepository;

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
                }
            } catch (Exception e) {
                log.error("공연 [ID: {}] 상태 업데이트 실패", festival.getId(), e);
            }
        });
    }
}
