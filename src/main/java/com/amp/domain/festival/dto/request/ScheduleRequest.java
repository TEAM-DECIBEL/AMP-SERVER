package com.amp.domain.festival.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleRequest {
    @NotNull(message = "공연 날짜는 필수입니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate festivalDate;

    @NotNull(message = "공연 시간은 필수입니다.")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime festivalTime;

    private String description;
}
