package com.amp.domain.festival.common.scheduler;

import com.amp.domain.festival.organizer.dto.request.ScheduleRequest;
import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.entity.FestivalSchedule;
import com.amp.domain.festival.common.exception.FestivalErrorCode;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FestivalScheduleService {

    public void syncSchedules(Festival festival, List<ScheduleRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new CustomException(FestivalErrorCode.SCHEDULES_REQUIRED);
        }

        List<FestivalSchedule> existSchedules = festival.getSchedules();
        Map<Long, FestivalSchedule> scheduleMap = existSchedules.stream()
                .collect(Collectors.toMap(FestivalSchedule::getId, Function.identity()));

        Set<Long> requestIdSet = requests.stream()
                .map(ScheduleRequest::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        existSchedules.removeIf(s -> !requestIdSet.contains(s.getId()));

        for (ScheduleRequest request : requests) {
            if (request.getId() != null && scheduleMap.containsKey(request.getId())) {
                scheduleMap.get(request.getId()).update(request.getFestivalDate(), request.getFestivalTime());
            } else {
                existSchedules.add(FestivalSchedule.builder()
                        .festival(festival)
                        .festivalDate(request.getFestivalDate())
                        .festivalTime(request.getFestivalTime())
                        .build());
            }
        }
    }
}
