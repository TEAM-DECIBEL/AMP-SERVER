package com.amp.domain.notice.dto.response;

public record Pagination(
        int currentPage,
        int totalPages,
        long totalElements,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {}