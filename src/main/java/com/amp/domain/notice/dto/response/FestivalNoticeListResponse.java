package com.amp.domain.notice.dto.response;

import java.util.List;

public record FestivalNoticeListResponse(
        Long noticeId,
        String categoryName,
        String title,
        String content,
        List<String> imageUrls,
        boolean isPinned,
        boolean isSaved,
        String createdAt
) {
}
