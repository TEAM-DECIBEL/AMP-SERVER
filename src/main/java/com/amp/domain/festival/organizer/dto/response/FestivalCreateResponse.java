package com.amp.domain.festival.organizer.dto.response;

import com.amp.domain.festival.common.entity.Festival;

import java.time.LocalDateTime;

public record FestivalCreateResponse(
        Long festivalId,
        String mainImageUrl,
        String title,
        LocalDateTime createdAt
) {
    public static FestivalCreateResponse from(Festival festival) {
        return new FestivalCreateResponse(
                festival.getId(),
                festival.getMainImageUrl(),
                festival.getTitle(),
                festival.getCreatedAt()
        );
    }
}