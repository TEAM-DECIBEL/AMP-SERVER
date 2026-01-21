package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.FestivalSchedule;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalTime;

public record FestivalScheduleResponse(
        Long id,
        @Schema(description = "공연 날짜", example = "2026-01-21")
        LocalDate festivalDate,
        @Schema(description = "공연 시간", example = "10:00", type = "string")
        LocalTime festivalTime
) {
    public static FestivalScheduleResponse from(FestivalSchedule festivalSchedule) {
        return new FestivalScheduleResponse(festivalSchedule.getId(), festivalSchedule.getFestivalDate(), festivalSchedule.getFestivalTime());
    }
}
