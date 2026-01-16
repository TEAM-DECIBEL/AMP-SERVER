package com.amp.domain.notice.common.dto.response;

import java.util.List;

public record NoticeListResponse(
        List<FestivalNoticeListResponse> announcements,
        Pagination pagination
) {}
