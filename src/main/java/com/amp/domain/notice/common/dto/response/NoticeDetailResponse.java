package com.amp.domain.notice.common.dto.response;

import java.time.LocalDateTime;

public record NoticeDetailResponse(
        Long noticeId,
        Long festivalId,
        String festivalTitle,
        CategoryData category,
        String title,
        String content,
        String imageUrl,
        boolean isPinned,
        boolean isSaved,
        Author author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

