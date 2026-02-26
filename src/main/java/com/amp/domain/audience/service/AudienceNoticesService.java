package com.amp.domain.audience.service;

import com.amp.domain.audience.dto.response.SavedNoticesResponse;
import com.amp.domain.notice.entity.Bookmark;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.global.common.dto.response.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudienceNoticesService {

    private final BookmarkRepository bookmarkRepository;

    public SavedNoticesResponse getSavedAnnouncements(Long userId, int page, int size) {
        log.info("저장한 공지 조회 - userId: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<Bookmark> bookmarksPage =
                bookmarkRepository.findByUserIdWithDetails(userId, pageable);

        List<SavedNoticesResponse.SavedAnnouncementDto> notices =
                bookmarksPage.getContent().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());

        return SavedNoticesResponse.builder()
                .notices(notices)
                .pagination(PaginationResponse.from(bookmarksPage))
                .build();
    }

    private SavedNoticesResponse.SavedAnnouncementDto convertToDto(Bookmark bookmark) {
        return SavedNoticesResponse.SavedAnnouncementDto.builder()
                .savedNoticeId(bookmark.getId())
                .noticeId(bookmark.getNotice().getId())
                .content(bookmark.getNotice().getContent())
                .festivalTitle(bookmark.getNotice().getFestival().getTitle())
                .categoryName(bookmark.getNotice().getFestivalCategory().getCategory().getCategoryName())
                .title(bookmark.getNotice().getTitle())
                .imageUrl(bookmark.getNotice().getImageUrl())
                .build();
    }
}
