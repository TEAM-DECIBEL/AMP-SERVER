package com.amp.domain.user.dto.response;

import com.amp.global.common.dto.response.PaginationResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SavedNoticesResponse {
    private List<SavedAnnouncementDto> notices;
    private PaginationResponse pagination;

    @Getter
    @Builder
    public static class SavedAnnouncementDto {
        private Long savedNoticeId;
        private Long noticeId;
        private String festivalTitle;
        private String categoryName;
        private String title;
        private String imageUrl;
    }
}
