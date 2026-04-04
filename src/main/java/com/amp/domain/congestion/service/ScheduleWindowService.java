package com.amp.domain.congestion.service;

import com.amp.domain.festival.entity.FestivalSchedule;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleWindowService {

    private final FestivalScheduleRepository festivalScheduleRepository;

    public boolean isWindowActive(Long festivalId) {
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        Optional<FestivalSchedule> todaySchedule =
                festivalScheduleRepository.findByFestivalIdAndFestivalDate(festivalId, today);
        if (todaySchedule.isPresent()) {
            return !now.isBefore(today.atTime(todaySchedule.get().getFestivalTime()).minusHours(8));
        }

        return festivalScheduleRepository.findByFestivalIdAndFestivalDate(festivalId, today.plusDays(1))
                .map(s -> !now.isBefore(today.plusDays(1).atTime(s.getFestivalTime()).minusHours(8)))
                .orElse(false);
    }
}
