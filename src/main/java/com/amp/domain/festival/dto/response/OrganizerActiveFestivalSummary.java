package com.amp.domain.festival.dto.response;

public record OrganizerActiveFestivalSummary(
        long ongoingCount,
        long upcomingCount,
        long totalCount
) {
}
