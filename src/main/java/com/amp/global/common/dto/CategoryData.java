package com.amp.global.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CategoryData {
    private final Long categoryId;
    private final String categoryName;
    private final String categoryCode;

    @Builder
    public CategoryData(Long categoryId, String categoryName, String categoryCode) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryCode = categoryCode;
    }
}
