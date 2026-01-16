package com.amp.domain.festival.organizer.dto.response;

public record OrganizerActiveFestivalSummary(
        long ongoingCount,
        long upcomingCount,
        long totalCount
) {
}
