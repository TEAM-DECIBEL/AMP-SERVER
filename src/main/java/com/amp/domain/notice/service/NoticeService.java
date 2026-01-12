package com.amp.domain.notice.service;

import com.amp.domain.notice.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.dto.response.Author;
import com.amp.domain.notice.dto.response.CategoryData;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.notice.repository.UserSavedNoticeRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
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

    public NoticeDetailResponse getNoticeDetail(Long noticeId, UserDetails userDetails) {

        // 공지 조회 (존재 검증 포함)
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() ->
                        new NoticeException(NoticeErrorCode.INVALID_NOTICE)
                );

        // 로그인 여부에 따른 저장 여부 판단
        boolean isSaved = false;

        if (userDetails != null) {
            //Todo 동찬한테 물어보고 반영
            Long userId = userDetails.getAuthorities().
                    ㅁ아ㅣㅁㄴ
                    너아ㅣㅁ어ㅣㅏㅁ;

            User user = userRepository.findById(userId)
                    .orElse(null);

            isSaved = userSavedNoticeRepository
                    .existsByNoticeAndUser(notice, user);
        }

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
}
