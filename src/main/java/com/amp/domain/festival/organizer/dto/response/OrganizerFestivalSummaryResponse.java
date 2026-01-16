package com.amp.domain.festival.organizer.dto.response;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.util.FestivalUtils;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrganizerFestivalSummaryResponse(
        Long festivalId,
        String mainImageUrl,
        String title,
        String period,
        String status,
        Long dDay

) {
    public static OrganizerFestivalSummaryResponse withDDay(Festival festival) {
        return new OrganizerFestivalSummaryResponse(
                festival.getId(),
                festival.getMainImageUrl(),
                festival.getTitle(),
                FestivalUtils.formatPeriod(festival.getStartDate(), festival.getEndDate()),
                festival.getStatus().getKoreanName(),
                FestivalUtils.calculateDDay(festival.getStartDate(), festival.getEndDate())
        );
    }

    public static OrganizerFestivalSummaryResponse from(Festival festival) {
        return new OrganizerFestivalSummaryResponse(
                festival.getId(),
                festival.getMainImageUrl(),
                festival.getTitle(),
                FestivalUtils.formatPeriod(festival.getStartDate(), festival.getEndDate()),
                festival.getStatus().getKoreanName(),
                null
        );
    }
}