package com.amp.domain.festival.scheduler;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.repository.FestivalRepository;
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

    @Scheduled(cron = "1 0 0 * * *")
    @Transactional
    public void updateFestivalStatus() {
        log.info("공연 상태 자동 업데이트 시작");

        List<Festival> activeFestivals = festivalRepository.findAllByStatusNot(FestivalStatus.COMPLETED);

        activeFestivals.forEach(festival -> {
            FestivalStatus oldStatus = festival.getStatus();
            festival.updateStatus();

            if (oldStatus != festival.getStatus()) {
                log.info("공연 [ID: {}] 상태 변경: {} -> {}",
                        festival.getId(), oldStatus, festival.getStatus());
            }
        });

        log.info("공연 상태 자동 업데이트 완료");
    }
}
