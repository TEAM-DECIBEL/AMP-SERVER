package com.amp.domain.notice.dto.response;

import java.util.List;

public record NoticeListResponse(
        List<FestivalNoticeListResponse> announcements,
        Pagination pagination
) {}
