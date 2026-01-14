package com.amp.domain.notice.dto.response;

public record BookmarkResponse(
        Long announcementId,
        boolean isBookmarked
) {
}
