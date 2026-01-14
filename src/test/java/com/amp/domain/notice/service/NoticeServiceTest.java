package com.amp.domain.notice.service;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.notice.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.entity.Bookmark;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.domain.user.entity.User;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @InjectMocks
    private NoticeService noticeService;

    private Festival festival;
    private Category category;
    private FestivalCategory festivalCategory;
    private Notice notice;
    private User author;
    private User loginUser;
    private Bookmark bookmark;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        String email = "loginUserMail@mail.com";

        festival = Festival.builder()
                .title("페스티벌 제목")
                .mainImageUrl("image.jpg")
                .location("서울")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .status(FestivalStatus.ONGOING)
                .build();

        category = Category.builder()
                .categoryName("공연")
                .build();

        festivalCategory = FestivalCategory.builder()
                .festival(festival)
                .category(category)
                .build();

        loginUser = User.builder()
                .id(1L)
                .email(email)
                .build();

        author = User.builder()
                .id(2L)
                .email("author@test.com")
                .nickname("작성자")
                .build();

        notice = Notice.builder()
                .title("공지 제목")
                .content("공지 내용")
                .festivalCategory(festivalCategory)
                .festival(festival)
                .user(author)
                .build();

        bookmark = Bookmark.builder()
                .notice(notice)
                .user(loginUser)
                .build();

    }

    @Test
    @DisplayName("비로그인 사용자는 isSaved가 false여야 한다")
    void notLoggedInUser_shouldHaveIsSavedFalse() {
        // given
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));

        // when
        NoticeDetailResponse response =
                noticeService.getNoticeDetail(1L);

        // then
        assertThat(response.isSaved()).isFalse();
    }

/*    @Test
    @DisplayName("로그인 사용자가 북마크한 공지는 isSaved가 true여야 한다")
    void loggedInUser_bookmarkedNotice_shouldHaveIsSavedTrue() {
        // given
        String email = "loginUserMail@mail.com";

*//*        User loginUser = User.builder()
                .email(email)
                .build();*//*

        Authentication auth =
                new UsernamePasswordAuthenticationToken(email, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(loginUser));

        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        when(userSavedNoticeRepository.existsByNoticeAndUser(notice, loginUser)).thenReturn(true);

        // when
        NoticeDetailResponse response =
                noticeService.getNoticeDetail(1L);

        // then
        assertThat(response.isSaved()).isTrue();
    }*/

    @Test
    @DisplayName("공지 조회 시 존재하지 않으면 예외 발생")
    void noticeNotFound_shouldThrowException() {
        // given
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() ->
                noticeService.getNoticeDetail(1L)
        )
                .isInstanceOf(NoticeException.class)
                .hasMessage("잘못된 공지 값입니다.");
    }

    @Test
    @DisplayName("공지 삭제 - 존재하지 않는 공지면 예외 발생")
    void deleteNotice_noticeNotFound_throwException() {
        // given
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() -> noticeService.deleteNotice(1L))
                .isInstanceOf(NoticeException.class)
                .hasMessage("잘못된 공지 값입니다.");
    }

    @Test
    @DisplayName("공지 삭제 - 이미 삭제된 공지면 예외 발생")
    void deleteNotice_alreadyDeleted_throwException() {
        // given
        notice.delete();

        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));

        // then
        assertThatThrownBy(() -> noticeService.deleteNotice(1L))
                .isInstanceOf(NoticeException.class)
                .hasMessage("이미 삭제된 공지입니다.");
    }

    @Test
    @DisplayName("공지 삭제 - 비로그인 사용자는 예외 발생")
    void deleteNotice_notLoggedInUser_throwException() {
        // given
        SecurityContextHolder.clearContext();

        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));

        // then
        assertThatThrownBy(() -> noticeService.deleteNotice(1L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("공지 삭제 - 작성자가 아닌 사용자는 삭제 불가")
    void deleteNotice_notAuthor_throwException() {
        // given
        String email = "loginUserMail@mail.com";

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of()
                );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(loginUser)); // loginUser ≠ author

        // then
        assertThatThrownBy(() -> noticeService.deleteNotice(1L))
                .isInstanceOf(NoticeException.class)
                .hasMessage("작성자 유저만 공지글을 삭제할 수 있습니다.");
    }


    @Test
    @DisplayName("공지 삭제 - 작성자가 삭제하면 deletedAt이 설정된다")
    void deleteNotice_author_success() {
        // given
        String email = "author@test.com";

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of()
                );
        SecurityContextHolder.getContext().setAuthentication(auth);


        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(author));

        // when
        noticeService.deleteNotice(1L);

        // then
        assertThat(notice.getDeletedAt()).isNotNull();
    }

}
