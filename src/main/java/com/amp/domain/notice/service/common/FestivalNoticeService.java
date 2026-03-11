package com.amp.domain.notice.service.common;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.notice.dto.response.FestivalNoticeListResponse;
import com.amp.domain.notice.dto.response.NoticeListResponse;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.entity.NoticeImage;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.user.repository.AudienceRepository;
import com.amp.global.common.dto.response.PaginationResponse;
import com.amp.global.security.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.amp.global.common.dto.TimeFormatter.formatTimeAgo;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class FestivalNoticeService {

    private final NoticeRepository noticeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AudienceRepository audienceRepository;
    private final FestivalRepository festivalRepository;

    private final AuthService authService;

    public NoticeListResponse getFestivalNoticeList(Long festivalId, Long categoryId, int page, int size) {

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new NoticeException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        Long filterCategoryId = (categoryId == null || categoryId == 0L) ? null : categoryId;

        Pageable pageable = PageRequest.of(page, size);

        Page<Notice> noticePage = noticeRepository.findNoticesByFilter(festival, filterCategoryId, pageable);
        Set<Long> savedNoticeIds = getSavedNoticeIds(noticePage.getContent());

        List<FestivalNoticeListResponse> announcements = noticePage.getContent().stream().map(notice -> {
            boolean isSaved = savedNoticeIds.contains(notice.getId());
            List<String> imageUrls = notice.getImages().stream()
                    .sorted(Comparator.comparingInt(NoticeImage::getImageOrder))
                    .map(NoticeImage::getImageUrl)
                    .toList();

            return new FestivalNoticeListResponse(
                    notice.getId(),
                    notice.getFestivalCategory().getCategory().getCategoryName(),
                    notice.getTitle(),
                    notice.getContent(),
                    imageUrls,
                    notice.getIsPinned(),
                    isSaved,
                    formatTimeAgo(notice.getCreatedAt())
            );
        }).collect(Collectors.toList());

        PaginationResponse pagination = PaginationResponse.from(noticePage);

        return new NoticeListResponse(announcements, pagination);
    }


    private Set<Long> getSavedNoticeIds(List<Notice> notices) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authService.isLoggedInUser(authentication)) {
            return Collections.emptySet();
        }
        String userEmail = authentication.getName();

        return audienceRepository.findByEmail(userEmail)
                .map(audience -> {
                    List<Long> noticeIds = notices.stream().map(Notice::getId).toList();
                    return new HashSet<>(bookmarkRepository.findNoticeIdsByAudienceAndNoticeIdIn(audience, noticeIds));
                })
                .orElse(new HashSet<>());
    }

}
