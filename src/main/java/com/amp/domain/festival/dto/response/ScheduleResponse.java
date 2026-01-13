package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.FestivalSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

public record ScheduleResponse(
        Long id,
        LocalDate festivalDate,
        LocalTime festivalTime
) {
    public static ScheduleResponse from(FestivalSchedule festivalSchedule) {
        return new ScheduleResponse(festivalSchedule.getId(), festivalSchedule.getFestivalDate(), festivalSchedule.getFestivalTime());
    }
}
