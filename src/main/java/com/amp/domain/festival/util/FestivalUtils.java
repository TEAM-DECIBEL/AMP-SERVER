package com.amp.domain.festival.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class FestivalUtils {
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");

    public static String formatPeriod(LocalDate startDate, LocalDate endDate) {
        String startStr = startDate.format(PERIOD_FORMATTER);
        String endStr = endDate.format(PERIOD_FORMATTER);

        if (startDate.equals(endDate)) {
            return startStr;
        }
        return String.format("%s - %s", startStr, endStr);
    }

    public static Long calculateDDay(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return ChronoUnit.DAYS.between(startDate, today);
        } else if (!today.isAfter(endDate)) {
            return 0L;
        } else {
            return ChronoUnit.DAYS.between(endDate, today);
        }
    }
}
