package com.amp.domain.wishList.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.util.FestivalUtils;

import java.time.LocalDate;

public record RecentWishListResponse(
        Long festivalId,
        String title,
        String mainImageUrl,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        Long dDay
) {
    public static RecentWishListResponse from(Festival festival) {
        return new RecentWishListResponse(
                festival.getId(),
                festival.getTitle(),
                festival.getMainImageUrl(),
                festival.getLocation(),
                festival.getStartDate(),
                festival.getEndDate(),
                FestivalUtils.calculateDDay(festival.getStartDate(), festival.getEndDate())
        );
    }
}
