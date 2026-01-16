package com.amp.domain.user.service;

import com.amp.domain.notice.entity.Bookmark;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.domain.user.dto.response.SavedAnnouncementResponse;
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
public class UserNoticesService {

    private final BookmarkRepository bookmarkRepository;


    public SavedAnnouncementResponse getSavedAnnouncements(Long userId, int page, int size) {
        log.info("저장한 공지 조회 - userId: {}, page: {}, size: {}", userId, page, size);

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page, size);

        // 저장한 공지 조회 (fetch join으로 N+1 방지)
        Page<Bookmark> bookmarksPage =
                bookmarkRepository.findByUserIdWithDetails(userId, pageable);

        // DTO 변환
        List<SavedAnnouncementResponse.SavedAnnouncementDto> notices =
                bookmarksPage.getContent().stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());

        // 페이지네이션 정보 생성
        SavedAnnouncementResponse.PaginationDto pagination = SavedAnnouncementResponse.PaginationDto.builder()
                .currentPage(bookmarksPage.getNumber())
                .totalPages(bookmarksPage.getTotalPages())
                .totalElements(bookmarksPage.getTotalElements())
                .size(bookmarksPage.getSize())
                .hasNext(bookmarksPage.hasNext())
                .hasPrevious(bookmarksPage.hasPrevious())
                .build();

        return SavedAnnouncementResponse.builder()
                .notices(notices)
                .pagination(pagination)
                .build();
    }


    private SavedAnnouncementResponse.SavedAnnouncementDto convertToDto(Bookmark bookmark) {
        return SavedAnnouncementResponse.SavedAnnouncementDto.builder()
                .savedNoticeId(bookmark.getId())
                .noticeId(bookmark.getNotice().getId())
                .festivalTitle(bookmark.getNotice().getFestival().getTitle())
                .categoryName(bookmark.getNotice().getFestivalCategory().getCategory().getCategoryName())
                .title(bookmark.getNotice().getTitle())
                .imageUrl(bookmark.getNotice().getImageUrl())
                .build();
    }
}
