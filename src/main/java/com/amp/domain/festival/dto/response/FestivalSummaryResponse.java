package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;

public record FestivalSummaryResponse(
        Long festivalId,
        String mainImageUrl,
        String title,
        String period,
        String status
) {
    public static FestivalSummaryResponse from(Festival festival) {
        return new FestivalSummaryResponse(
                festival.getId(),
                festival.getMainImageUrl(),
                festival.getTitle(),
                String.format("%s ~ %s", festival.getStartDate(), festival.getEndDate()),
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
