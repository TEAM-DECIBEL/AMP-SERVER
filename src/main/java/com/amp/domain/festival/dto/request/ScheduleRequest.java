package com.amp.domain.festival.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "공연 날짜", example = "2026-01-21")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate festivalDate;

    @NotNull(message = "공연 시간은 필수입니다.")
    @Schema(description = "공연 시간", example = "10:00", type = "string")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime festivalTime;
}
