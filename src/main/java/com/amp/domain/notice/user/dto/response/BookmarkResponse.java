package com.amp.domain.notice.user.dto.response;

public record BookmarkResponse(
        Long noticeId,
        boolean isBookmarked
) {
}
