package com.amp.domain.wishList.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.entity.UserFestival;
import com.amp.domain.festival.util.FestivalUtils;

public record MyWishListResponse(
        Long festivalId,
        String title,
        String mainImageUrl,
        String period,
        String status,
        boolean wishList,
        Long dDay
) {
    public static MyWishListResponse from(UserFestival userFestival) {
        Festival festival = userFestival.getFestival();
        return new MyWishListResponse(
                festival.getId(),
                festival.getTitle(),
                festival.getMainImageUrl(),
                FestivalUtils.formatPeriod(festival.getStartDate(), festival.getEndDate()),
                convertToUserStatus(festival.getStatus()),
                userFestival.getWishList(),
                FestivalUtils.calculateDDay(festival.getStartDate(), festival.getEndDate())
        );
    }

    private static String convertToUserStatus(FestivalStatus status) {
        return switch (status) {
            case UPCOMING -> "관람 예정";
            case ONGOING -> "관람 중";
            case COMPLETED -> "관람 완료";
            default -> status.getKoreanName();
        };
    }
}
