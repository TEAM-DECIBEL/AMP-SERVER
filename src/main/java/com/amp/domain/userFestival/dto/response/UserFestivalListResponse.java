package com.amp.domain.userFestival.dto.response;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.util.FestivalUtils;

public record UserFestivalListResponse(
        Long festivalId,
        String title,
        String mainImageUrl,
        String period,
        boolean wishList,
        long dDay
) {
    public static UserFestivalListResponse from(Festival festival, boolean isWishList) {
        return new UserFestivalListResponse(
                festival.getId(),
                festival.getTitle(),
                festival.getMainImageUrl(),
                FestivalUtils.formatPeriod(festival.getStartDate(), festival.getEndDate()),
                isWishList,
                FestivalUtils.calculateDDay(festival.getStartDate(), festival.getEndDate())
        );
    }
}
