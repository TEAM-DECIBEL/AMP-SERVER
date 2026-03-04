package com.amp.domain.congestion.dto.request;

import com.amp.domain.congestion.entity.CongestionLevel;
import jakarta.validation.constraints.NotNull;

public record CongestionReportRequest(
        @NotNull(message = "혼잡도 입력은 필수입니다.") CongestionLevel congestionLevel
) {
}
