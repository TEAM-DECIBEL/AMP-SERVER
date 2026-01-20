package com.amp.domain.notice.dto.response;

import com.amp.global.common.dto.PaginationResponse;

import java.util.List;

public record NoticeListResponse(
        List<FestivalNoticeListResponse> announcements,
        PaginationResponse paginationResponse
) {
}
