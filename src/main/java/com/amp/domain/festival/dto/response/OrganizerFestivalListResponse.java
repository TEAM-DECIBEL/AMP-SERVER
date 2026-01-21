package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.util.FestivalUtils;

public record OrganizerFestivalListResponse(
        String imageUrl,
        String title,
        String period,
        String status
) {
    public static OrganizerFestivalListResponse from(Festival festival) {
        return new OrganizerFestivalListResponse(
                festival.getMainImageUrl(),
                festival.getTitle(),
                FestivalUtils.formatPeriod(festival.getStartDate(), festival.getEndDate()),
                convertToKorean(festival.getStatus())
        );
    }

    private static String convertToKorean(FestivalStatus status) {
        return switch (status) {
            case UPCOMING -> "진행 예정";
            case ONGOING -> "진행 중";
            case COMPLETED -> "진행 완료";
            default -> "알 수 없음";
        };
    }
}
