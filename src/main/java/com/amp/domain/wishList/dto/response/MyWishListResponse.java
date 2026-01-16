package com.amp.domain.wishList.dto.response;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.entity.FestivalStatus;
import com.amp.domain.festival.common.entity.UserFestival;
import com.amp.domain.festival.common.util.FestivalUtils;

public record MyWishListResponse(
        Long festivalId,
        String title,
        String mainImageUrl,
        String period,
        String status,
        boolean wishList
) {
    public static MyWishListResponse from(UserFestival uf) {
        Festival f = uf.getFestival();
        return new MyWishListResponse(
                f.getId(),
                f.getTitle(),
                f.getMainImageUrl(),
                FestivalUtils.formatPeriod(f.getStartDate(), f.getEndDate()),
                convertToUserStatus(f.getStatus()),
                uf.getWishList()
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
