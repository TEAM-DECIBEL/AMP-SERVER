package com.amp.domain.notice.service;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.exception.FestivalErrorCode;
import com.amp.domain.festival.common.repository.FestivalRepository;
import com.amp.domain.notice.dto.response.FestivalNoticeListResponse;
import com.amp.domain.notice.dto.response.NoticeListResponse;
import com.amp.domain.notice.dto.response.Pagination;
import com.amp.domain.notice.common.entity.Notice;
import com.amp.domain.notice.common.exception.NoticeException;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class FestivalNoticeService {

    private final NoticeRepository noticeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final FestivalRepository festivalRepository;

    private final AuthService authService;

    public NoticeListResponse getFestivalNoticeList(Long festivalId, int page, int size) {

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new NoticeException(FestivalErrorCode.FESTIVAL_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size);

        Page<Notice> noticePage = noticeRepository.findAllByFestival(festival, pageable);

        Set<Long> savedNoticeIds = getSavedNoticeIds(noticePage.getContent());

        List<FestivalNoticeListResponse> announcements = noticePage.getContent().stream().map(notice -> {
            boolean isSaved = savedNoticeIds.contains(notice.getId());
            return new FestivalNoticeListResponse(
                    notice.getId(),
                    notice.getFestivalCategory().getCategory().getCategoryName(),
                    notice.getTitle(),
                    notice.getContent(),
                    notice.getImageUrl(),
                    notice.getIsPinned(),
                    isSaved,
                    formatTimeAgo(notice.getCreatedAt())
            );
        }).collect(Collectors.toList());

        Pagination pagination = new Pagination(
                noticePage.getNumber(),
                noticePage.getTotalPages(),
                noticePage.getTotalElements(),
                noticePage.getSize(),
                noticePage.hasNext(),
                noticePage.hasPrevious()
        );

        return new NoticeListResponse(announcements, pagination);
    }


    private Set<Long> getSavedNoticeIds(List<Notice> notices) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authService.isLoggedInUser(authentication)) {
            return Collections.emptySet();
        }
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        List<Long> noticeIds = notices.stream().map(Notice::getId).toList();
        return new HashSet<>(bookmarkRepository.findNoticeIdsByUserAndNoticeIdIn(user, noticeIds));
    }

    private String formatTimeAgo(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) {
            return "방금 전";
        }
        if (minutes < 60) {
            return minutes + "분 전";
        }
        if (hours < 24) {
            return hours + "시간 전";
        }
        return days + "일 전";
    }


}
