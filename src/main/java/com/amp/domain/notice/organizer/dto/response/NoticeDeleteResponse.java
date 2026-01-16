package com.amp.domain.notice.organizer.dto.response;

public record NoticeDeleteResponse(
    Long noticeId,
    boolean isBookmarked
) {
}
