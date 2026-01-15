package com.amp.global.common.dto;

import org.springframework.data.domain.Page;

public record PaginationResponse(
        int currentPage,
        int totalPages,
        long totalElements,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PaginationResponse from(Page<T> page) {
        return new PaginationResponse(
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
