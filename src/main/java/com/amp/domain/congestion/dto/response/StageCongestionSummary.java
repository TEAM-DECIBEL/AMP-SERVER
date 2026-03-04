package com.amp.domain.congestion.dto.response;

import com.amp.domain.congestion.entity.CongestionLevel;
import lombok.Builder;

@Builder
public record StageCongestionSummary(
        Long stageId,
        String title,
        String location,
        CongestionLevel congestionLevel
) {
}
