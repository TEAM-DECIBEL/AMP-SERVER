package com.amp.domain.wishList.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.entity.UserFestival;
import com.amp.domain.festival.util.FestivalUtils;
import com.amp.domain.wishList.util.WishListUtils;

public record WishListHistoryResponse(
        Long festivalId,
        String title,
        String mainImageUrl,
        String period,
        String status,
        boolean wishList
) {
    public static WishListHistoryResponse from(UserFestival userFestival) {
        Festival festival = userFestival.getFestival();
        return new WishListHistoryResponse(
                festival.getId(),
                festival.getTitle(),
                festival.getMainImageUrl(),
                FestivalUtils.formatPeriod(festival.getStartDate(), festival.getEndDate()),
                WishListUtils.convertToUserStatus(festival.getStatus()),
                userFestival.getWishList()
        );
    }

}
