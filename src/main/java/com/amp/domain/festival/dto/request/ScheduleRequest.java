package com.amp.domain.festival.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class ScheduleRequest {
    private Long id;

    @NotNull(message = "공연 날짜는 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate festivalDate;

    @NotNull(message = "공연 시간은 필수입니다.")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime festivalTime;
}
