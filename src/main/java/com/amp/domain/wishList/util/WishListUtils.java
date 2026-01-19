package com.amp.domain.wishList.util;

import com.amp.domain.festival.entity.FestivalStatus;

public class WishListUtils {

    public static String convertToUserStatus(FestivalStatus status) {
        return switch (status) {
            case UPCOMING -> "관람 예정";
            case ONGOING -> "관람 중";
            case COMPLETED -> "관람 완료";
            default -> status.getKoreanName();
        };
    }
}
