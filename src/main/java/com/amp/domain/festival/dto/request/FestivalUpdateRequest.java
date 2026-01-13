package com.amp.domain.festival.dto.request;

import com.amp.domain.stage.dto.request.StageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FestivalUpdateRequest(
        @NotBlank(message = "공연명은 필수입니다.") String title,
        @NotBlank(message = "공연 장소는 필수입니다.") String location,
        @NotBlank(message = "공연 일시는 필수입니다.") List<ScheduleRequest> schedules,
        List<StageRequest> stages,
        @NotEmpty(message = "최소 1개의 카테고리를 선택해야 합니다.") List<Long> activeCategoryIds
) {
}
