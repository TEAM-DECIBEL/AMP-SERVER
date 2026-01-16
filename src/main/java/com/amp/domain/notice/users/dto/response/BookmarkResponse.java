package com.amp.domain.notice.users.dto.response;

public record BookmarkResponse(
        Long noticeId,
        boolean isBookmarked
) {
}
