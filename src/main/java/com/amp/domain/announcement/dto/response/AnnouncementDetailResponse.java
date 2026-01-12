package com.amp.domain.announcement.dto.response;

import java.time.LocalDateTime;

public record AnnouncementDetailResponse(
        Long announcementId,
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

