package com.amp.domain.stage.dto.response;

import com.amp.global.common.dto.PaginationResponse;
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
            List<StageCongestionSummary> stages,
            Page<?> page
    ) {
        return FestivalCongestionResponse.builder()
                .isInputAvailable(isInputAvailable)
                .stages(stages)
                .pagination(PaginationResponse.from(page))
                .build();
    }
}
