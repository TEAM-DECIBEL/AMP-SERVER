package com.amp.domain.wishList.dto.response;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.util.FestivalUtils;

public record WishListSummaryResponse(
        Long festivalId,
        String title,
        String mainImageUrl,
        String period,
        boolean wishList,
        long dDay
) {
    public static WishListSummaryResponse from(Festival festival, boolean isWishList) {
        return new WishListSummaryResponse(
                festival.getId(),
                festival.getTitle(),
                festival.getMainImageUrl(),
                FestivalUtils.formatPeriod(festival.getStartDate(), festival.getEndDate()),
                isWishList,
                FestivalUtils.calculateDDay(festival.getStartDate(), festival.getEndDate())
        );
    }
}
