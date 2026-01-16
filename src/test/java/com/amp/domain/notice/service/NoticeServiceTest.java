package com.amp.domain.notice.service;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.entity.FestivalStatus;
import com.amp.domain.festival.common.repository.FestivalRepository;
import com.amp.domain.notice.common.service.FestivalNoticeService;
import com.amp.domain.notice.organizer.dto.request.NoticeCreateRequest;
import com.amp.domain.notice.organizer.dto.response.NoticeCreateResponse;
import com.amp.domain.notice.common.dto.response.NoticeDetailResponse;
import com.amp.domain.notice.common.dto.response.NoticeListResponse;
import com.amp.domain.notice.common.entity.Bookmark;
import com.amp.domain.notice.common.entity.Notice;
import com.amp.domain.notice.common.exception.NoticeException;
import com.amp.domain.notice.common.repository.BookmarkRepository;
import com.amp.domain.notice.common.repository.NoticeRepository;
import com.amp.domain.notice.organizer.service.NoticeService;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3Service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private FestivalCategoryRepository festivalCategoryRepository;

    @Mock
    private OrganizerRepository organizerRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private NoticeService noticeService;

    @InjectMocks
    private FestivalNoticeService festivalNoticeService;

    @Mock
    private FestivalRepository festivalRepository;

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

        ReflectionTestUtils.setField(festival, "id", 1L);

        category = Category.builder()
                .categoryName("공연")
                .build();

        festivalCategory = FestivalCategory.builder()
                .festival(festival)
                .category(category)
                .build();

        ReflectionTestUtils.setField(festivalCategory, "id", 1L);

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
    @DisplayName("공지 작성 - 정상적인 주최자는 공지를 작성할 수 있다")
    void createNotice_success() {
        // given
        String email = "loginUserMail@mail.com";

        Authentication auth =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        NoticeCreateRequest request = new NoticeCreateRequest(
                "공지 제목",
                "1",
                null,
                "내용",
                true
        );

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(loginUser));

        when(festivalRepository.findById(1L))
                .thenReturn(Optional.of(festival));

        when(organizerRepository.existsByFestivalAndUser(festival, loginUser))
                .thenReturn(true);

        when(festivalCategoryRepository.findById(1L))
                .thenReturn(Optional.of(festivalCategory));

        when(noticeRepository.save(any(Notice.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        NoticeCreateResponse response =
                noticeService.createNotice(1L, request);

        assertThat(response).isNotNull();

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
                .hasMessage("존재하지 않는 공지 아이디입니다.");
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
                .hasMessage("존재하지 않는 공지 아이디입니다.");
    }

    @Test
    @DisplayName("공지 삭제 - 이미 삭제된 공지면 예외 발생")
    void deleteNotice_alreadyDeleted_throwException() {
        // given
        notice.delete();

        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));

        String email = "author@test.com";
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

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

    @Test
    @DisplayName("페스티벌 공지 목록 조회 성공")
    void getFestivalNoticeList_success() {
        // given
        Festival festival = mock(Festival.class);

        Category category = mock(Category.class);
        when(category.getCategoryName()).thenReturn("전체");

        FestivalCategory festivalCategory = mock(FestivalCategory.class);
        when(festivalCategory.getCategory()).thenReturn(category);

        Notice notice = mock(Notice.class);
        when(notice.getId()).thenReturn(1L);
        when(notice.getTitle()).thenReturn("공지 제목");
        when(notice.getContent()).thenReturn("공지 내용");
        when(notice.getImageUrl()).thenReturn(null);
        when(notice.getIsPinned()).thenReturn(true);
        when(notice.getFestivalCategory()).thenReturn(festivalCategory);
        when(notice.getCreatedAt()).thenReturn(LocalDateTime.now().minusMinutes(5));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Notice> noticePage = new PageImpl<>(List.of(notice), pageable, 1);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(noticeRepository.findAllByFestival(festival, pageable)).thenReturn(noticePage);

        // when
        NoticeListResponse response =
                festivalNoticeService.getFestivalNoticeList(1L, 0, 10);

        // then
        assertThat(response.announcements()).hasSize(1);

        var announcement = response.announcements().get(0);
        assertThat(announcement.noticeId()).isEqualTo(1L);
        assertThat(announcement.categoryName()).isEqualTo("전체");
        assertThat(announcement.title()).isEqualTo("공지 제목");
        assertThat(announcement.isPinned()).isTrue();
        assertThat(announcement.isSaved()).isFalse();
        assertThat(announcement.createdAt()).contains("분 전");

        assertThat(response.pagination().currentPage()).isEqualTo(0);
        assertThat(response.pagination().totalPages()).isEqualTo(1);
        assertThat(response.pagination().totalElements()).isEqualTo(1);
        assertThat(response.pagination().hasNext()).isFalse();
    }

    @Test
    @DisplayName("페스티벌이 존재하지 않으면 예외 발생")
    void getFestivalNoticeList_festivalNotFound() {
        // given
        when(festivalRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                festivalNoticeService.getFestivalNoticeList(1L, 0, 10)
        ).isInstanceOf(NoticeException.class);
    }

    @Test
    @DisplayName("비로그인 사용자일 경우 isSaved는 false")
    void getFestivalNoticeList_notLoggedIn_isSavedFalse() {
        // given
        Festival festival = mock(Festival.class);

        FestivalCategory festivalCategory = mock(FestivalCategory.class);
        Category category = mock(Category.class);
        when(category.getCategoryName()).thenReturn("전체");
        when(festivalCategory.getCategory()).thenReturn(category);

        Notice notice = mock(Notice.class);
        when(notice.getFestivalCategory()).thenReturn(festivalCategory);
        when(notice.getCreatedAt()).thenReturn(LocalDateTime.now().minusHours(1));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Notice> noticePage = new PageImpl<>(List.of(notice), pageable, 1);

        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(noticeRepository.findAllByFestival(festival, pageable)).thenReturn(noticePage);

        // when
        NoticeListResponse response =
                festivalNoticeService.getFestivalNoticeList(1L, 0, 10);

        // then
        assertThat(response.announcements().get(0).isSaved()).isFalse();
    }
}
