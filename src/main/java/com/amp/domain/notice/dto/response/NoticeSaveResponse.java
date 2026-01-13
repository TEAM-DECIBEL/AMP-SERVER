package com.amp.domain.notice.dto.response;

public record NoticeSaveResponse(
        Long announcementId,
        boolean isBookmarked
) {
}
