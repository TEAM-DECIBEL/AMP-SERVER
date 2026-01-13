package com.amp.domain.notice.service;

import com.amp.domain.notice.dto.request.NoticeSaveRequest;
import com.amp.domain.notice.dto.response.NoticeSaveResponse;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.entity.UserSavedNotice;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.exception.UserSavedNoticeErrorCode;
import com.amp.domain.notice.exception.UserSavedNoticeException;
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
public class UserSavedNoticeService {

    private final UserSavedNoticeRepository userSavedNoticeRepository;
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;

    public NoticeSaveResponse saveNotice(Long noticeId, NoticeSaveRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND));
        UserSavedNotice userSavedNotice = userSavedNoticeRepository
                .findByNoticeAndUser(notice, user)
                .orElse(null);
        boolean isSaved;

        if (request.isBookmarked()) {
            if (userSavedNotice != null) {
                throw new UserSavedNoticeException(UserSavedNoticeErrorCode.NOTICE_ALREADY_BOOKMARKED);
            }
            userSavedNoticeRepository.save(new UserSavedNotice(user, notice));
            isSaved = true;

        } else {
            if (userSavedNotice == null) {
                throw new UserSavedNoticeException(UserSavedNoticeErrorCode.SAVED_NOTICE_NOT_EXIST);
            }
            userSavedNoticeRepository.delete(userSavedNotice);
            isSaved = false;
        }


        return new NoticeSaveResponse(
                notice.getId(),
                isSaved
        );
    }

}
