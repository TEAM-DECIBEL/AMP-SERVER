package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FestivalSummaryResponse(
        Long festivalId,
        String mainImageUrl,
        String title,
        String period,
        String status,
        Long dDay

) {
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");

    public static FestivalSummaryResponse withDDay(Festival festival) {
        return new FestivalSummaryResponse(
                festival.getId(),
                festival.getMainImageUrl(),
                festival.getTitle(),
                formatPeriod(festival),
                festival.getStatus().getKoreanName(),
                calculateDDay(festival)
        );
    }

    public static FestivalSummaryResponse from(Festival festival) {
        return new FestivalSummaryResponse(
                festival.getId(),
                festival.getMainImageUrl(),
                festival.getTitle(),
                formatPeriod(festival),
                festival.getStatus().getKoreanName(),
                null
        );
    }

    private static Long calculateDDay(Festival festival) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = festival.getStartDate();
        LocalDate endDate = festival.getEndDate();

        if (today.isBefore(startDate)) {
            return ChronoUnit.DAYS.between(startDate, today);
        } else if (today.isAfter(endDate)) {
            return ChronoUnit.DAYS.between(endDate, today);
        } else {
            return 0L;
        }
    }

    private static String formatPeriod(Festival festival) {
        String startStr = festival.getStartDate().format(PERIOD_FORMATTER);
        String endStr = festival.getEndDate().format(PERIOD_FORMATTER);

        if (festival.getStartDate().equals(festival.getEndDate())) {
            return startStr;
        }

        return String.format("%s - %s", startStr, endStr);
    }
}
