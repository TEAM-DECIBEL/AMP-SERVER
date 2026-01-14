package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
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
    public static FestivalSummaryResponse withDDay(Festival festival) {
        return new FestivalSummaryResponse(
                festival.getId(),
                festival.getMainImageUrl(),
                festival.getTitle(),
                formatPeriod(festival),
                convertToKorean(festival.getStatus()),
                calculateDDay(festival)
        );
    }

    public static FestivalSummaryResponse from(Festival festival) {
        return new FestivalSummaryResponse(
                festival.getId(),
                festival.getMainImageUrl(),
                festival.getTitle(),
                formatPeriod(festival),
                convertToKorean(festival.getStatus()),
                null
        );
    }

    private static String convertToKorean(FestivalStatus status) {
        return switch (status) {
            case UPCOMING -> "진행 예정";
            case ONGOING -> "진행 중";
            case COMPLETED -> "진행 완료";
            default -> "알 수 없음";
        };
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");
        String startStr = festival.getStartDate().format(formatter);
        String endStr = festival.getEndDate().format(formatter);

        if (festival.getStartDate().equals(festival.getEndDate())) {
            return startStr;
        }

        return String.format("%s - %s", startStr, endStr);
    }
}
