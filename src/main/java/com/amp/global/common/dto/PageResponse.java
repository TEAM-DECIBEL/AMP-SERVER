package com.amp.global.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        @JsonProperty("festivals")
        List<T> content,
        PaginationResponse pagination
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                PaginationResponse.from(page)
        );
    }
}