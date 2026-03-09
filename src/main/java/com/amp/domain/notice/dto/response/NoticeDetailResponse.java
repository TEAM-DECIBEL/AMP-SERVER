package com.amp.domain.notice.dto.response;

import com.amp.global.common.dto.CategoryData;

import java.util.List;

public record NoticeDetailResponse(
        Long noticeId,
        Long festivalId,
        String festivalTitle,
        CategoryData category,
        String title,
        String content,
        List<String> imageUrls,
        boolean isPinned,
        boolean isSaved,
        Author author,
        String createdAt
) {
}
