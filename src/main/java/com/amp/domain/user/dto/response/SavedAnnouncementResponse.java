package com.amp.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SavedAnnouncementResponse {
    private List<SavedAnnouncementDto> notices;
    private PaginationDto pagination;

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

    @Getter
    @Builder
    public static class PaginationDto {
        private int currentPage;
        private int totalPages;
        private long totalElements;
        private int size;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}
