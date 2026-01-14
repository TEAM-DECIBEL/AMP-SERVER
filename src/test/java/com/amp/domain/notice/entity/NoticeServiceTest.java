package com.amp.domain.notice.entity;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.notice.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.notice.repository.BookmarkRepository;
import com.amp.domain.notice.service.NoticeService;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
                .email(email)
                .build();

        author = User.builder()
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
}
