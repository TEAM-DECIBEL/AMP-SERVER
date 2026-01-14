package com.amp.domain.notice.dto.response;

public record NoticeDeleteResponse(
    Long noticeId,
    boolean isBookmarked
) {
}
