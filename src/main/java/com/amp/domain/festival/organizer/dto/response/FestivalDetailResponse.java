package com.amp.domain.festival.organizer.dto.response;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.stage.dto.response.StageResponse;

import java.util.List;

public record FestivalDetailResponse(
        Long festivalId,
        String title,
        String location,
        String mainImageUrl,
        List<FestivalScheduleResponse> schedules,
        List<StageResponse> stages,
        List<Long> activeCategoryIds
) {
    public static FestivalDetailResponse from(Festival festival) {
        return new FestivalDetailResponse(
                festival.getId(),
                festival.getTitle(),
                festival.getLocation(),
                festival.getMainImageUrl(),
                festival.getSchedules().stream().map(FestivalScheduleResponse::from).toList(),
                festival.getStages().stream().map(StageResponse::from).toList(),
                festival.getFestivalCategories().stream()
                        .filter(FestivalCategory::isActive)
                        .map(fc -> fc.getCategory().getId()).toList()
        );
    }
}
