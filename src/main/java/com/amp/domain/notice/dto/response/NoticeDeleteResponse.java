package com.amp.domain.notice.dto.response;

public record NoticeDeleteResponse(
    long announcementId,
    boolean isBookmarked
) {
}
