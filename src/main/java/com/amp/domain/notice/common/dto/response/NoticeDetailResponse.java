package com.amp.domain.notice.common.dto.response;

import com.amp.domain.notice.dto.response.Author;
import com.amp.domain.notice.dto.response.CategoryData;

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

