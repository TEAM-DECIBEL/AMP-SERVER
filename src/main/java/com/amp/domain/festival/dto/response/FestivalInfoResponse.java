package com.amp.domain.festival.dto.response;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.util.FestivalUtils;
import com.amp.domain.notice.dto.response.CategoryData;

import java.util.List;

public record FestivalInfoResponse(
        Long festivalId,
        String title,
        String location,
        String period,
        Boolean isWishlist,
        Long dday,
        List<CategoryData> activeCategories) {
    public static FestivalInfoResponse from(Festival festival, Boolean
            isWishlist) {
        return new FestivalInfoResponse(
                festival.getId(),
                festival.getTitle(),
                festival.getLocation(),
                FestivalUtils.formatPeriod(festival.getStartDate(), festival.getEndDate()),
                isWishlist,
                FestivalUtils.calculateDDay(festival.getStartDate(), festival.getEndDate()),
                festival.getFestivalCategories().stream()
                        .filter(FestivalCategory::isActive)
                        .map(fc -> CategoryData.builder()
                                .categoryId(fc.getCategory().getId())
                                .categoryName(fc.getCategory().getCategoryName())
                                .categoryCode(fc.getCategory().getCategoryCode())
                                .build())
                        .toList()
        );
    }
}

