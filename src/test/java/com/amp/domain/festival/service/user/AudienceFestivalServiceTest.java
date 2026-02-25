package com.amp.domain.festival.service.user;

import com.amp.domain.festival.dto.response.AudienceFestivalSummaryResponse;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.wishList.repository.WishListRepository;
import com.amp.global.common.dto.response.PageResponse;
import com.amp.global.security.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AudienceFestivalService 테스트")
class AudienceFestivalServiceTest {

    @Mock
    private WishListRepository userFestivalRepository;

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AudienceFestivalService audienceFestivalService;

    @Nested
    @DisplayName("getAllFestivals - LocalDate.now() 파라미터 전달 검증")
    class GetAllFestivals {

        @Test
        @DisplayName("오늘 날짜(LocalDate.now())를 today 파라미터로 전달한다")
        void passesLocalDateNow_AsTodayParam() {
            // given
            given(authService.getCurrentUserOrNull()).willReturn(null);
            given(festivalRepository.findActiveFestivals(any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());

            Pageable pageable = PageRequest.of(0, 10);

            // when
            audienceFestivalService.getAllFestivals(pageable);

            // then - LocalDate.now()가 today 파라미터로 전달됐는지 검증
            ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
            then(festivalRepository).should().findActiveFestivals(eq(pageable), dateCaptor.capture());

            assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("비로그인 사용자: 빈 위시리스트 Set으로 처리한다")
        void nonLoggedInUser_EmptyWishlistSet() {
            // given - 비로그인 (null 반환)
            given(authService.getCurrentUserOrNull()).willReturn(null);
            given(festivalRepository.findActiveFestivals(any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());

            // when
            PageResponse<AudienceFestivalSummaryResponse> result =
                    audienceFestivalService.getAllFestivals(PageRequest.of(0, 10));

            // then - 위시리스트 ID 조회가 호출되지 않아야 함
            then(userFestivalRepository).should(never()).findAllFestivalIdsByUserId(anyLong());
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
        }

        @Test
        @DisplayName("로그인 사용자: 위시리스트 ID 조회가 수행된다")
        void loggedInUser_FetchesWishlistIds() {
            // given
            User user = createUser(1L);
            given(authService.getCurrentUserOrNull()).willReturn(user);
            given(festivalRepository.findActiveFestivals(any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());
            given(userFestivalRepository.findAllFestivalIdsByUserId(1L))
                    .willReturn(Set.of(10L, 20L));

            // when
            audienceFestivalService.getAllFestivals(PageRequest.of(0, 10));

            // then - 로그인 사용자의 위시리스트 ID 조회 호출 확인
            then(userFestivalRepository).should().findAllFestivalIdsByUserId(eq(1L));
        }

        @Test
        @DisplayName("로그인 사용자: 위시리스트가 없는 경우 빈 Set으로 처리한다")
        void loggedInUser_EmptyWishlist() {
            // given
            User user = createUser(1L);
            given(authService.getCurrentUserOrNull()).willReturn(user);
            given(festivalRepository.findActiveFestivals(any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());
            given(userFestivalRepository.findAllFestivalIdsByUserId(1L))
                    .willReturn(Collections.emptySet());

            // when
            PageResponse<AudienceFestivalSummaryResponse> result =
                    audienceFestivalService.getAllFestivals(PageRequest.of(0, 10));

            // then
            assertThat(result.content()).isEmpty();
        }

        @Test
        @DisplayName("페이지 정보가 그대로 전달된다")
        void paginationIsPassedThrough() {
            // given
            given(authService.getCurrentUserOrNull()).willReturn(null);
            given(festivalRepository.findActiveFestivals(any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());

            Pageable pageable = PageRequest.of(3, 20);

            // when
            audienceFestivalService.getAllFestivals(pageable);

            // then - 정확한 Pageable 객체가 전달됨
            then(festivalRepository).should().findActiveFestivals(eq(pageable), any(LocalDate.class));
        }

        @Test
        @DisplayName("결과를 PageResponse로 변환하여 반환한다")
        void returnsPageResponse() {
            // given
            given(authService.getCurrentUserOrNull()).willReturn(null);
            given(festivalRepository.findActiveFestivals(any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());

            // when
            PageResponse<AudienceFestivalSummaryResponse> result =
                    audienceFestivalService.getAllFestivals(PageRequest.of(0, 10));

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
        }
    }

    // ===== 헬퍼 메서드 =====

    private User createUser(Long id) {
        User user = User.builder()
                .email("user" + id + "@test.com")
                .nickname("테스트유저")
                .profileImageUrl("https://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_user" + id)
                .isActive(true)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.AUDIENCE)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
