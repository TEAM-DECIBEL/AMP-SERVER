package com.amp.domain.stage.dto.response;

import com.amp.domain.stage.entity.CongestionLevel;
import lombok.Builder;

@Builder
public record StageCongestionSummary(
        Long stageId,
        String title,
        String location,
        CongestionLevel congestionLevel
) {
}
