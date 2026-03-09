package com.amp.domain.audience.dto.response;

import com.amp.domain.notice.entity.Bookmark;
import com.amp.domain.notice.entity.NoticeImage;
import com.amp.global.common.dto.response.PaginationResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.Comparator;
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
        private String content;
        private String title;
        private List<String> imageUrls;

        public static SavedAnnouncementDto from(Bookmark bookmark) {
            return SavedAnnouncementDto.builder()
                    .savedNoticeId(bookmark.getId())
                    .noticeId(bookmark.getNotice().getId())
                    .content(bookmark.getNotice().getContent())
                    .festivalTitle(bookmark.getNotice().getFestival().getTitle())
                    .categoryName(bookmark.getNotice().getFestivalCategory().getCategory().getCategoryName())
                    .title(bookmark.getNotice().getTitle())
                    .imageUrls(bookmark.getNotice().getImages().stream()
                            .sorted(Comparator.comparingInt(NoticeImage::getImageOrder))
                            .map(NoticeImage::getImageUrl)
                            .toList())
                    .build();
        }
    }
}
