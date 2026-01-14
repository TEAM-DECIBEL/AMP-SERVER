package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record OrganizerActiveFestivalPageResponse(
        OrganizerActiveFestivalSummary summary,
        List<OrganizerFestivalSummaryResponse> ongoingFestivals,
        List<OrganizerFestivalSummaryResponse> upcomingFestivals,
        PaginationResponse paginationResponse
) {
    public static OrganizerActiveFestivalPageResponse of(
            long ongoingCount,
            long upcomingCount,
            Page<Festival> festivalPage
    ) {
        List<OrganizerFestivalSummaryResponse> allItems = festivalPage.getContent().stream()
                .map(OrganizerFestivalSummaryResponse::withDDay)
                .toList();

        List<OrganizerFestivalSummaryResponse> ongoing = allItems.stream()
                .filter(dto -> dto.status().equals(FestivalStatus.ONGOING.getKoreanName()))
                .toList();

        List<OrganizerFestivalSummaryResponse> upcoming = allItems.stream()
                .filter(dto -> dto.status().equals(FestivalStatus.UPCOMING.getKoreanName()))
                .toList();

        return new OrganizerActiveFestivalPageResponse(
                new OrganizerActiveFestivalSummary(ongoingCount, upcomingCount, ongoingCount + upcomingCount),
                ongoing,
                upcoming,
                PaginationResponse.from(festivalPage)
        );
    }
}
