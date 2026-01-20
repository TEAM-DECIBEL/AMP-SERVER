package com.amp.domain.notice.dto.response;

public record FestivalNoticeListResponse(
        Long noticeId,
        String categoryName,
        String title,
        String content,
        String imageUrl,
        boolean isPinned,
        boolean isSaved,
        String createdAt
) {
}

