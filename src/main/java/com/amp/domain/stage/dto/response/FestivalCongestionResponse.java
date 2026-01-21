package com.amp.domain.stage.dto.response;

import com.amp.global.common.dto.response.PaginationResponse;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.util.List;

@Builder
public record FestivalCongestionResponse(
        boolean isInputAvailable,
        List<StageCongestionSummary> stages,
        PaginationResponse pagination
) {
    public static FestivalCongestionResponse of(
            boolean isInputAvailable,
            Page<StageCongestionSummary> page
    ) {
        return FestivalCongestionResponse.builder()
                .isInputAvailable(isInputAvailable)
                .stages(page.getContent())
                .pagination(PaginationResponse.from(page))
                .build();
    }
}
