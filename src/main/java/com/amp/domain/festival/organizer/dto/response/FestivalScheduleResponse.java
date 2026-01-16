package com.amp.domain.festival.organizer.dto.response;

import com.amp.domain.festival.common.entity.FestivalSchedule;

import java.time.LocalDate;
import java.time.LocalTime;

public record FestivalScheduleResponse(
        Long id,
        LocalDate festivalDate,
        LocalTime festivalTime
) {
    public static FestivalScheduleResponse from(FestivalSchedule festivalSchedule) {
        return new FestivalScheduleResponse(festivalSchedule.getId(), festivalSchedule.getFestivalDate(), festivalSchedule.getFestivalTime());
    }
}
