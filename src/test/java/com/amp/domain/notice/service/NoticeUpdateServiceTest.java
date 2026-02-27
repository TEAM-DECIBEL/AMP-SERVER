package com.amp.domain.notice.service;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.notice.dto.request.NoticeUpdateRequest;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.NoticeRepository;
import com.amp.domain.notice.service.organizer.NoticeUpdateService;
import com.amp.domain.user.entity.Organizer;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.OrganizerRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeUpdateServiceTest {

    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private OrganizerRepository organizerRepository;
    @Mock
    private FestivalCategoryRepository festivalCategoryRepository;
    @Mock
    private FestivalRepository festivalRepository;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private NoticeUpdateService noticeUpdateService;

    private Festival festival;
    private FestivalCategory festivalCategory;
    private Notice notice;
    private Organizer organizer;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        organizer = Organizer.builder()
                .id(1L)
                .email("organizer@test.com")
                .build();

        festival = Festival.builder()
                .title("축제")
                .status(FestivalStatus.ONGOING)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build();
        ReflectionTestUtils.setField(festival, "id", 1L);
        ReflectionTestUtils.setField(festival, "organizer", organizer);

        Category category = Category.builder()
                .categoryName("공지")
                .build();

        festivalCategory = FestivalCategory.builder()
                .festival(festival)
                .category(category)
                .build();
        ReflectionTestUtils.setField(festivalCategory, "id", 1L);

        notice = Notice.builder()
                .title("기존 제목")
                .content("기존 내용")
                .festival(festival)
                .festivalCategory(festivalCategory)
                .organizer(organizer)
                .imageUrl("old-image-url")
                .build();
        ReflectionTestUtils.setField(notice, "id", 1L);
    }

    @Test
    @DisplayName("공지 수정 성공 - 주최자는 공지를 수정할 수 있다")
    void updateNoticeSuccess() {
        // given
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        organizer.getEmail(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        NoticeUpdateRequest request = new NoticeUpdateRequest(
                1L,                 // festivalId
                "수정된 제목",
                1L,                // categoryId
                null,               // newImage
                "수정된 내용",
                true,
                "http://image.jpg"  // previousImageUrl
        );

        when(organizerRepository.findByEmail(organizer.getEmail()))
                .thenReturn(Optional.of(organizer));
        when(festivalRepository.findById(1L))
                .thenReturn(Optional.of(festival));
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));
        when(festivalCategoryRepository.findById(1L))
                .thenReturn(Optional.of(festivalCategory));

        // when
        noticeUpdateService.updateNotice(1L, request);

        // then
        assertThat(notice.getTitle()).isEqualTo("수정된 제목");
        assertThat(notice.getContent()).isEqualTo("수정된 내용");
        assertThat(notice.getIsPinned()).isTrue();
    }

    @Test
    @DisplayName("비로그인 사용자는 공지 수정 불가")
    void updateNoticeNotLoggedInThrowException() {
        // given
        SecurityContextHolder.clearContext();

        NoticeUpdateRequest request = new NoticeUpdateRequest(
                1L, "제목", 1L, null, "내용", false, null
        );

        // then
        assertThatThrownBy(() ->
                noticeUpdateService.updateNotice(1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMsg());
    }

    @Test
    @DisplayName("주최자가 아니면 공지 수정 불가")
    void updateNoticeNotOrganizerThrowException() {
        // given
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        organizer.getEmail(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        NoticeUpdateRequest request =
                new NoticeUpdateRequest(
                        1L, "제목", 1L, null, "내용", false, null);

        Organizer differentOrganizer = Organizer.builder()
                .id(99L)
                .email(organizer.getEmail())
                .build();

        when(organizerRepository.findByEmail(organizer.getEmail()))
                .thenReturn(Optional.of(differentOrganizer));
        when(festivalRepository.findById(1L))
                .thenReturn(Optional.of(festival));

        // then
        assertThatThrownBy(() ->
                noticeUpdateService.updateNotice(1L, request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(UserErrorCode.USER_NOT_AUTHORIZED.getMsg());
    }


    @Test
    @DisplayName("존재하지 않는 공지면 예외 발생")
    void updateNoticeNoticeNotFoundThrowException() {
        // given
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        organizer.getEmail(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        NoticeUpdateRequest request =
                new NoticeUpdateRequest(
                        1L,
                        "수정된 제목",
                        1L,
                        null,
                        "수정된 내용",
                        true,
                        "http://image.jpg"
                );
        when(organizerRepository.findByEmail(organizer.getEmail()))
                .thenReturn(Optional.of(organizer));
        when(festivalRepository.findById(1L))
                .thenReturn(Optional.of(festival));
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.empty());

        // then
        assertThatThrownBy(() ->
                noticeUpdateService.updateNotice(1L, request))
                .isInstanceOf(NoticeException.class);
    }

    @Test
    @DisplayName("삭제된 공지는 수정할 수 없다")
    void updateNoticeDeletedNoticeThrowException() {
        // given
        notice.delete();

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        organizer.getEmail(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        NoticeUpdateRequest request =
                new NoticeUpdateRequest(
                        1L,
                        "수정된 제목",
                        1L,
                        null,
                        "수정된 내용",
                        true,
                        "http://image.jpg"
                );
        when(organizerRepository.findByEmail(organizer.getEmail()))
                .thenReturn(Optional.of(organizer));
        when(festivalRepository.findById(1L))
                .thenReturn(Optional.of(festival));
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));

        // then
        assertThatThrownBy(() ->
                noticeUpdateService.updateNotice(1L, request))
                .isInstanceOf(NoticeException.class);
    }

    @Test
    @DisplayName("이미지 포함 수정 시 S3 업로드가 수행된다")
    void updateNoticeWithImageSuccess() {
        // given
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        organizer.getEmail(), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockMultipartFile image =
                new MockMultipartFile(
                        "image", "test.png", "image/png", "test".getBytes());

        NoticeUpdateRequest request =
                new NoticeUpdateRequest(
                        1L,
                        "수정된 제목",
                        1L,
                        image,
                        "수정된 내용",
                        true,
                        "http://image.jpg"
                );
        when(organizerRepository.findByEmail(organizer.getEmail()))
                .thenReturn(Optional.of(organizer));
        when(festivalRepository.findById(1L))
                .thenReturn(Optional.of(festival));
        when(noticeRepository.findById(1L))
                .thenReturn(Optional.of(notice));
        when(festivalCategoryRepository.findById(1L))
                .thenReturn(Optional.of(festivalCategory));
        when(s3Service.upload(any(), any()))
                .thenReturn("new-image-key");
        when(s3Service.getPublicUrl("new-image-key"))
                .thenReturn("new-image-url");

        // when
        noticeUpdateService.updateNotice(1L, request);

        // then
        assertThat(notice.getImageUrl()).isEqualTo("new-image-url");
    }
}
