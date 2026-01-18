package com.amp.domain.stage.dto.response;

import lombok.Builder;

@Builder
public record StageCongestionSummary(
        Long stageId,
        String title,
        String location,
        String congestionLevel
) {
}
