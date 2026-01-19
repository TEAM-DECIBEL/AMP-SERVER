package com.amp.domain.notice.dto.response;

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
        String createdAt
) {
}

