package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record ActiveFestivalPageResponse(
        ActiveFestivalSummary summary,
        List<FestivalSummaryResponse> ongoingFestivals,
        List<FestivalSummaryResponse> upcomingFestivals,
        PaginationResponse paginationResponse
) {
    public static ActiveFestivalPageResponse of(
            long ongoingCount,
            long upcomingCount,
            Page<Festival> festivalPage
    ) {
        List<FestivalSummaryResponse> allItems = festivalPage.getContent().stream()
                .map(FestivalSummaryResponse::withDDay)
                .toList();

        List<FestivalSummaryResponse> ongoing = allItems.stream()
                .filter(dto -> dto.status().equals("진행 중"))
                .toList();

        List<FestivalSummaryResponse> upcoming = allItems.stream()
                .filter(dto -> dto.status().equals("진행 예정"))
                .toList();

        return new ActiveFestivalPageResponse(
                new ActiveFestivalSummary(ongoingCount, upcomingCount, ongoingCount + upcomingCount),
                ongoing,
                upcoming,
                PaginationResponse.from(festivalPage)
        );
    }
}

record ActiveFestivalSummary(
        long ongoingCount,
        long upcomingCount,
        long totalCount
) {
}