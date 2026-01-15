package com.amp.domain.notice.dto.response;

import java.time.LocalDateTime;

public record FestivalNoticeListResponse(
        Long noticeId,
        String categoryName,
        String title,
        String content,
        String imageUrl,
        boolean isPinned,
        boolean isSaved,
        LocalDateTime createdAt
) {
}

