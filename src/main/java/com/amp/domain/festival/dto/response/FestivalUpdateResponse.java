package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;

import java.time.LocalDateTime;

public record FestivalUpdateResponse(
        Long festivalId,
        LocalDateTime updatedAt
) {
    public static FestivalUpdateResponse from(Festival festival) {
        return new FestivalUpdateResponse(
                festival.getId(),
                festival.getUpdatedAt()
        );
    }
}
