package com.amp.domain.notice.service;

import com.amp.domain.notice.dto.response.Author;
import com.amp.domain.notice.dto.response.CategoryData;
import com.amp.domain.notice.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.notice.repository.UserSavedNoticeRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserSavedNoticeRepository userSavedNoticeRepository;
    private final UserRepository userRepository;

    public NoticeDetailResponse getNoticeDetail(Long noticeId) {

        // 공지 조회 (존재 검증 포함)
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
                );

        // 로그인 여부에 따른 저장 여부 판단
        boolean isSaved = getIsSaved(notice);

        CategoryData category = new CategoryData(
                notice.getFestivalCategory().getId(),
                notice.getFestivalCategory().getCategory().getCategoryName(),
                notice.getFestivalCategory().getCategory().getCategoryCode()
        );

        Author author = new Author(
                notice.getUser().getId(),
                notice.getUser().getNickname()
        );

        return new NoticeDetailResponse(
                notice.getId(),
                notice.getFestival().getId(),
                notice.getFestival().getTitle(),
                category,
                notice.getTitle(),
                notice.getContent(),
                notice.getImageUrl(),
                notice.getIsPinned(),
                isSaved,
                author,
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

    private boolean getIsSaved(Notice notice) {
        boolean isSaved = false;

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // 로그인한 사용자만 북마크 여부 확인
        if (isLoggedInUser(authentication)) {
            String userEmail = authentication.getName();
            User user = userRepository.findByEmail(userEmail).orElseThrow(() ->
                    new CustomException(UserErrorCode.USER_NOT_FOUND));


            isSaved = userSavedNoticeRepository
                    .existsByNoticeAndUser(notice, user);
        }
        return isSaved;
    }

    private boolean isLoggedInUser(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }
}
