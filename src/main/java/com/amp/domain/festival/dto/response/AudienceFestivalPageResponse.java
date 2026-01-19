package com.amp.domain.festival.dto.response;

import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record AudienceFestivalPageResponse(
        List<AudienceFestivalSummaryResponse> festivals,
        PaginationResponse pagination
) {
    public static AudienceFestivalPageResponse of(Page<AudienceFestivalSummaryResponse> page) {
        return new AudienceFestivalPageResponse(page.getContent(), PaginationResponse.from(page));
    }

    public boolean isEmpty() {
        return festivals().isEmpty();
    }
}
