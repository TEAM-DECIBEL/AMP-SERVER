package com.amp.domain.notice.users.service;

import com.amp.domain.notice.users.dto.request.BookmarkRequest;
import com.amp.domain.notice.users.dto.response.BookmarkResponse;
import com.amp.domain.notice.common.entity.Bookmark;
import com.amp.domain.notice.common.entity.Notice;
import com.amp.domain.notice.common.exception.BookmarkErrorCode;
import com.amp.domain.notice.common.exception.BookmarkException;
import com.amp.domain.notice.common.exception.NoticeErrorCode;
import com.amp.domain.notice.common.exception.NoticeException;
import com.amp.domain.notice.common.repository.BookmarkRepository;
import com.amp.domain.notice.common.repository.NoticeRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;

    public BookmarkResponse updateBookmark(Long noticeId, BookmarkRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND));
        Bookmark bookmark = bookmarkRepository
                .findByNoticeAndUser(notice, user)
                .orElse(null);
        boolean isSaved;

        if (request.isBookmarked()) {
            if (bookmark != null) {
                throw new BookmarkException(BookmarkErrorCode.NOTICE_ALREADY_BOOKMARKED);
            }
            bookmarkRepository.save(new Bookmark(user, notice));
            isSaved = true;

        } else {
            if (bookmark == null) {
                throw new BookmarkException(BookmarkErrorCode.SAVED_NOTICE_NOT_EXIST);
            }
            bookmarkRepository.delete(bookmark);
            isSaved = false;
        }


        return new BookmarkResponse(
                notice.getId(),
                isSaved
        );
    }

}
