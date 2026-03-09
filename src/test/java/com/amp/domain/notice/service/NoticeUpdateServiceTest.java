package com.amp.domain.notice.service;

import com.amp.domain.category.entity.Category;
import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.notice.dto.request.NoticeUpdateRequest;
import com.amp.domain.notice.entity.Notice;
import com.amp.domain.notice.entity.NoticeImage;
import com.amp.domain.notice.exception.NoticeErrorCode;
import com.amp.domain.notice.exception.NoticeException;
import com.amp.domain.notice.repository.NoticeImageRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeUpdateServiceTest {

    @Mock private NoticeRepository noticeRepository;
    @Mock private NoticeImageRepository noticeImageRepository;
    @Mock private OrganizerRepository organizerRepository;
    @Mock private FestivalCategoryRepository festivalCategoryRepository;
    @Mock private FestivalRepository festivalRepository;
    @Mock private S3Service s3Service;

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
                .categoryName("공연")
                .categoryCode("PERFORMANCE")
                .build();
        ReflectionTestUtils.setField(category, "id", 10L);

        festivalCategory = FestivalCategory.builder()
                .festival(festival)
                .category(category)
                .build();
        ReflectionTestUtils.setField(festivalCategory, "id", 1L);

        notice = Notice.builder()
                .title("기존 제목")
                .content("기존 내용")
                .isPinned(false)
                .festival(festival)
                .festivalCategory(festivalCategory)
                .organizer(organizer)
                .build();
        ReflectionTestUtils.setField(notice, "id", 1L);
        ReflectionTestUtils.setField(notice, "images", new ArrayList<>());
    }

    private void setAuth(String email) {
        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void stubCommonMocks() {
        when(organizerRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));
        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        when(festivalCategoryRepository.findByMapping(1L, 10L)).thenReturn(Optional.of(festivalCategory));
    }

    @Test
    @DisplayName("공지 수정 - 비로그인 사용자는 수정 불가")
    void updateNoticeNotLoggedInThrowException() {
        // given
        SecurityContextHolder.clearContext();
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, null, "내용", false);

        // when & then
        assertThatThrownBy(() -> noticeUpdateService.updateNotice(1L, request, null))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.USER_NOT_FOUND.getMsg());
    }

    @Test
    @DisplayName("공지 수정 - 다른 페스티벌 주최자는 수정 불가")
    void updateNoticeDifferentOrganizerThrowException() {
        // given
        Organizer other = Organizer.builder().id(99L).email("other@test.com").build();
        setAuth(other.getEmail());
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, null, "내용", false);
        when(organizerRepository.findByEmail(other.getEmail())).thenReturn(Optional.of(other));
        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));

        // when & then
        assertThatThrownBy(() -> noticeUpdateService.updateNotice(1L, request, null))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.USER_NOT_AUTHORIZED.getMsg());
    }

    @Test
    @DisplayName("공지 수정 - 삭제된 공지는 수정 불가")
    void updateNoticeDeletedNoticeThrowException() {
        // given
        notice.delete();
        setAuth(organizer.getEmail());
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, null, "내용", false);
        when(organizerRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));
        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));

        // when & then
        assertThatThrownBy(() -> noticeUpdateService.updateNotice(1L, request, null))
                .isInstanceOf(NoticeException.class)
                .hasMessage(NoticeErrorCode.NOTICE_ALREADY_DELETED.getMsg());
    }

    @Test
    @DisplayName("공지 수정 - 이미지 없이 텍스트만 수정")
    void updateNoticeTextOnlySuccess() {
        // given
        setAuth(organizer.getEmail());
        stubCommonMocks();
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "수정된 제목", 10L, null, "수정된 내용", true);

        // when
        noticeUpdateService.updateNotice(1L, request, null);

        // then
        assertThat(notice.getTitle()).isEqualTo("수정된 제목");
        assertThat(notice.getContent()).isEqualTo("수정된 내용");
        assertThat(notice.getIsPinned()).isTrue();
    }

    @Test
    @DisplayName("공지 수정 - keepImageUrls=null이면 기존 이미지 전체 삭제")
    void updateNoticeKeepUrlsNullDeleteAllImages() {
        // given
        List<NoticeImage> images = new ArrayList<>();
        images.add(NoticeImage.of(notice, "https://bucket/img1.jpg", 0));
        images.add(NoticeImage.of(notice, "https://bucket/img2.jpg", 1));
        ReflectionTestUtils.setField(notice, "images", images);
        setAuth(organizer.getEmail());
        stubCommonMocks();
        when(s3Service.extractKey(anyString())).thenReturn("notices/img.jpg");
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, null, "내용", false);

        // when
        noticeUpdateService.updateNotice(1L, request, null);

        // then
        verify(s3Service, times(2)).delete(anyString());
        assertThat(notice.getImages()).isEmpty();
    }

    @Test
    @DisplayName("공지 수정 - keepImageUrls로 일부 유지, 나머지 삭제")
    void updateNoticeKeepSomeImagesDeleteOthers() {
        // given
        List<NoticeImage> images = new ArrayList<>();
        images.add(NoticeImage.of(notice, "https://bucket/img1.jpg", 0));
        images.add(NoticeImage.of(notice, "https://bucket/img2.jpg", 1));
        images.add(NoticeImage.of(notice, "https://bucket/img3.jpg", 2));
        ReflectionTestUtils.setField(notice, "images", images);
        setAuth(organizer.getEmail());
        stubCommonMocks();
        when(s3Service.extractKey("https://bucket/img2.jpg")).thenReturn("notices/img2.jpg");

        // img1, img3 유지 → img2만 삭제
        List<String> keepUrls = List.of("https://bucket/img1.jpg", "https://bucket/img3.jpg");
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, keepUrls, "내용", false);

        // when
        noticeUpdateService.updateNotice(1L, request, null);

        // then
        verify(s3Service, times(1)).delete("notices/img2.jpg");
        assertThat(notice.getImages()).hasSize(2);
    }

    @Test
    @DisplayName("공지 수정 - keepImageUrls 순서대로 imageOrder 재정렬")
    void updateNoticeKeepUrlsReorderedByKeepUrlsOrder() {
        // given
        NoticeImage imgA = NoticeImage.of(notice, "https://bucket/A.jpg", 0);
        NoticeImage imgB = NoticeImage.of(notice, "https://bucket/B.jpg", 1);
        NoticeImage imgC = NoticeImage.of(notice, "https://bucket/C.jpg", 2);
        List<NoticeImage> images = new ArrayList<>(List.of(imgA, imgB, imgC));
        ReflectionTestUtils.setField(notice, "images", images);
        setAuth(organizer.getEmail());
        stubCommonMocks();

        // B → A → C 순서로 재정렬 요청
        List<String> keepUrls = List.of("https://bucket/B.jpg", "https://bucket/A.jpg", "https://bucket/C.jpg");
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, keepUrls, "내용", false);

        // when
        noticeUpdateService.updateNotice(1L, request, null);

        // then
        assertThat(imgB.getImageOrder()).isEqualTo(0);
        assertThat(imgA.getImageOrder()).isEqualTo(1);
        assertThat(imgC.getImageOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("공지 수정 - 새 이미지는 유지 이미지 뒤 순서로 저장된다")
    void updateNoticeNewImagesOrderAfterKeptImages() {
        // given
        NoticeImage existing = NoticeImage.of(notice, "https://bucket/existing.jpg", 0);
        List<NoticeImage> images = new ArrayList<>(List.of(existing));
        ReflectionTestUtils.setField(notice, "images", images);
        setAuth(organizer.getEmail());
        stubCommonMocks();
        when(s3Service.upload(any(), anyString())).thenReturn("new-key");
        when(s3Service.getPublicUrl("new-key")).thenReturn("https://bucket/new.jpg");

        List<String> keepUrls = List.of("https://bucket/existing.jpg");
        List<MultipartFile> newImages = List.of(
                new MockMultipartFile("img", "new.jpg", "image/jpeg", "data".getBytes())
        );
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, keepUrls, "내용", false);

        // when
        noticeUpdateService.updateNotice(1L, request, newImages);

        // then - 기존 1장(order=0) 뒤에 새 이미지(order=1)로 저장
        verify(noticeImageRepository).save(argThat(img -> img.getImageOrder() == 1));
    }

    @Test
    @DisplayName("공지 수정 - keep 2개 + new 19개 = 21장이면 예외 발생")
    void updateNoticeImageTotalExceeds20ThrowException() {
        // given
        setAuth(organizer.getEmail());
        stubCommonMocks();
        List<String> keepUrls = List.of("https://bucket/img1.jpg", "https://bucket/img2.jpg");
        List<MultipartFile> newImages = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            newImages.add(new MockMultipartFile("img", "img.jpg", "image/jpeg", "data".getBytes()));
        }
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, keepUrls, "내용", false);

        // when & then
        assertThatThrownBy(() -> noticeUpdateService.updateNotice(1L, request, newImages))
                .isInstanceOf(NoticeException.class)
                .hasMessage(NoticeErrorCode.NOTICE_IMAGE_LIMIT_EXCEEDED.getMsg());
    }

    @Test
    @DisplayName("공지 수정 - 새 이미지 S3 업로드 실패 시 이미 업로드된 이미지 롤백")
    void updateNoticeS3UploadFailRollbackUploadedImages() {
        // given
        setAuth(organizer.getEmail());
        stubCommonMocks();
        List<MultipartFile> newImages = List.of(
                new MockMultipartFile("img1", "a.jpg", "image/jpeg", "data".getBytes()),
                new MockMultipartFile("img2", "b.jpg", "image/jpeg", "data".getBytes())
        );
        when(s3Service.upload(any(), anyString()))
                .thenReturn("key1")
                .thenThrow(new RuntimeException("S3 오류"));
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, null, "내용", false);

        // when & then
        assertThatThrownBy(() -> noticeUpdateService.updateNotice(1L, request, newImages))
                .isInstanceOf(NoticeException.class);
        verify(s3Service).delete("key1");
    }

    @Test
    @DisplayName("공지 수정 - 비고정 → 고정 전환 시 이미 3개면 예외 발생")
    void updateNoticePinLimitExceededThrowException() {
        // given
        setAuth(organizer.getEmail());
        when(organizerRepository.findByEmail(organizer.getEmail())).thenReturn(Optional.of(organizer));
        when(festivalRepository.findById(1L)).thenReturn(Optional.of(festival));
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(notice));
        when(noticeRepository.countByFestivalAndIsPinnedTrueAndDeletedAtIsNull(festival)).thenReturn(3L);
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, null, "내용", true);

        // when & then
        assertThatThrownBy(() -> noticeUpdateService.updateNotice(1L, request, null))
                .isInstanceOf(NoticeException.class)
                .hasMessage(NoticeErrorCode.PINNED_NOTICE_LIMIT_EXCEEDED.getMsg());
    }

    @Test
    @DisplayName("공지 수정 - 이미 고정 상태에서 고정 유지 시 카운트 체크 안 함")
    void updateNoticeAlreadyPinnedNoCountCheck() {
        // given
        ReflectionTestUtils.setField(notice, "isPinned", true);
        setAuth(organizer.getEmail());
        stubCommonMocks();
        NoticeUpdateRequest request = new NoticeUpdateRequest(1L, "제목", 10L, null, "내용", true);

        // when
        noticeUpdateService.updateNotice(1L, request, null);

        // then
        verify(noticeRepository, never()).countByFestivalAndIsPinnedTrueAndDeletedAtIsNull(any());
    }
}
