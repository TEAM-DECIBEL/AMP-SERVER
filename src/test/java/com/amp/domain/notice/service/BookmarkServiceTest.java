package com.amp.domain.notice.service;

import com.amp.domain.notice.dto.request.BookmarkRequest;
import com.amp.domain.notice.dto.response.BookmarkResponse;
import com.amp.domain.notice.entity.Bookmark;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.exception.BookmarkException;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private BookmarkService bookmarkService;

    private User loginUser;
    private Notice notice;
    private Bookmark bookmark;

    private final String email = "loginUser@test.com";

    @BeforeEach
    void setUp() {
        loginUser = User.builder()
                .email(email)
                .build();

        notice = Notice.builder()
                .title("공지 제목")
                .content("공지 내용")
                .user(loginUser)
                .build();

        bookmark = Bookmark.builder()
                .user(loginUser)
                .notice(notice)
                .build();

        // SecurityContext 로그인 세팅
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("북마크 저장 성공")
    void bookmarkSave_success() {
        // given
        BookmarkRequest request = new BookmarkRequest(true);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(loginUser));
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));
        when(bookmarkRepository.findByNoticeAndUser(notice, loginUser))
                .thenReturn(Optional.empty());

        // when
        BookmarkResponse response =
                bookmarkService.updateBookmark(1L, request);

        // then
        assertThat(response.isBookmarked()).isTrue();
        verify(bookmarkRepository).save(any(Bookmark.class));
    }

    @Test
    @DisplayName("이미 북마크한 공지를 다시 저장하면 예외 발생")
    void bookmarkAlreadyExists_shouldThrowException() {
        // given
        BookmarkRequest request = new BookmarkRequest(true);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(loginUser));
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));
        when(bookmarkRepository.findByNoticeAndUser(notice, loginUser))
                .thenReturn(Optional.of(bookmark));

        // then
        assertThatThrownBy(() ->
                bookmarkService.updateBookmark(1L, request)
        ).isInstanceOf(BookmarkException.class);
    }

    @Test
    @DisplayName("북마크 삭제 성공")
    void bookmarkDelete_success() {
        // given
        BookmarkRequest request = new BookmarkRequest(false);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(loginUser));
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));
        when(bookmarkRepository.findByNoticeAndUser(notice, loginUser))
                .thenReturn(Optional.of(bookmark));

        // when
        BookmarkResponse response =
                bookmarkService.updateBookmark(1L, request);

        // then
        assertThat(response.isBookmarked()).isFalse();
        verify(bookmarkRepository).delete(bookmark);
    }

    @Test
    @DisplayName("북마크가 없는데 삭제 요청하면 예외 발생")
    void bookmarkNotExists_shouldThrowException() {
        // given
        BookmarkRequest request = new BookmarkRequest(false);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(loginUser));
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));
        when(bookmarkRepository.findByNoticeAndUser(notice, loginUser))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() ->
                bookmarkService.updateBookmark(1L, request)
        ).isInstanceOf(BookmarkException.class);
    }

    @Test
    @DisplayName("로그인 유저가 DB에 없으면 예외 발생")
    void userNotFound_shouldThrowException() {
        // given
        BookmarkRequest request = new BookmarkRequest(true);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() ->
                bookmarkService.updateBookmark(1L, request)
        )
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMsg());
    }

    @Test
    @DisplayName("공지 존재하지 않으면 예외 발생")
    void noticeNotFound_shouldThrowException() {
        // given
        BookmarkRequest request = new BookmarkRequest(true);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(loginUser));
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() ->
                bookmarkService.updateBookmark(1L, request)
        ).isInstanceOf(NoticeException.class);
    }
}
