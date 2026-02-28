package com.amp.domain.audience.service;

import com.amp.domain.audience.dto.response.SavedNoticesResponse;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.global.common.dto.response.PaginationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudienceNoticesService {

    private final BookmarkRepository bookmarkRepository;

    public SavedNoticesResponse getSavedAnnouncements(Long userId, int page, int size) {
        log.info("저장한 공지 조회 - userId: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<SavedNoticesResponse.SavedAnnouncementDto> dtoPage =
                bookmarkRepository.findByAudienceIdWithDetails(userId, pageable)
                        .map(SavedNoticesResponse.SavedAnnouncementDto::from);

        return SavedNoticesResponse.builder()
                .notices(dtoPage.getContent())
                .pagination(PaginationResponse.from(dtoPage))
                .build();
    }
}
