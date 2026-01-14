package com.amp.domain.festival.dto.response;

import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record FestivalPageResponse(
        List<FestivalSummaryResponse> festivals,
        PaginationResponse pagination
) {
    public static FestivalPageResponse of(Page<FestivalSummaryResponse> page) {
        return new FestivalPageResponse(
                page.getContent(),
                PaginationResponse.from(page)
        );
    }
}
