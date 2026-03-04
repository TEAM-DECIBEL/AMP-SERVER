package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.util.FestivalUtils;

public record AudienceFestivalSummaryResponse(
        Long festivalId,
        String title,
        String mainImageUrl,
        String period,
        String status,

        boolean wishList,
        long dDay
) {
    public static AudienceFestivalSummaryResponse from(Festival festival, boolean isWishList) {
        return new AudienceFestivalSummaryResponse(
                festival.getId(),
                festival.getTitle(),
                festival.getMainImageUrl(),
                FestivalUtils.formatPeriod(festival.getStartDate(), festival.getEndDate()),
                "관람 예정",
                isWishList,
                FestivalUtils.calculateDDay(festival.getStartDate(), festival.getEndDate())
        );
    }
}
