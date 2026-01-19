package com.amp.global.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> festivals,
        PaginationResponse pagination
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                PaginationResponse.from(page)
        );
    }
}