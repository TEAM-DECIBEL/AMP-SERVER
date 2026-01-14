package com.amp.domain.festival.dto.response;

public record ActiveFestivalSummary(
        long ongoingCount,
        long upcomingCount,
        long totalCount
) {
}
