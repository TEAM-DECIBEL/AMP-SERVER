package com.amp.domain.festival.dto.response;

import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record OrganizerFestivalPageResponse(
        List<OrganizerFestivalSummaryResponse> festivals,
        PaginationResponse pagination
) {
    public static OrganizerFestivalPageResponse of(Page<OrganizerFestivalSummaryResponse> page) {
        return new OrganizerFestivalPageResponse(
                page.getContent(),
                PaginationResponse.from(page)
        );
    }
}
