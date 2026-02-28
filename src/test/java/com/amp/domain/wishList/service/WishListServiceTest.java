package com.amp.domain.wishList.service;

import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.wishList.dto.response.MyUpcomingResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WishListService 테스트")
class WishListServiceTest {

    @Mock
    private WishListRepository wishListRepository;

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private WishListService wishListService;

    @Nested
    @DisplayName("getMyWishList - LocalDate.now() 파라미터 전달 검증")
    class GetMyWishList {

        @Test
        @DisplayName("오늘 날짜(LocalDate.now())를 today 파라미터로 전달한다")
        void passesLocalDateNowAsTodayParam() {
            // given
            User user = createUser(1L);
            given(authService.getCurrentUser()).willReturn(user);
            given(wishListRepository.findAllByUserIdAndWishListTrue(
                    anyLong(), any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());

            Pageable pageable = PageRequest.of(0, 10);

            // when
            wishListService.getMyWishList(pageable);

            // then - LocalDate.now()가 today 파라미터로 전달됐는지 검증
            ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
            then(wishListRepository).should().findAllByUserIdAndWishListTrue(
                    eq(1L), eq(pageable), dateCaptor.capture());

            assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("현재 로그인한 사용자의 ID로 위시리스트를 조회한다")
        void queriesWithCurrentUserId() {
            // given
            User user = createUser(42L);
            given(authService.getCurrentUser()).willReturn(user);
            given(wishListRepository.findAllByUserIdAndWishListTrue(
                    anyLong(), any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());

            // when
            wishListService.getMyWishList(PageRequest.of(0, 10));

            // then
            then(wishListRepository).should().findAllByUserIdAndWishListTrue(
                    eq(42L), any(Pageable.class), any(LocalDate.class));
        }

        @Test
        @DisplayName("결과를 PageResponse로 변환하여 반환한다")
        void returnsPageResponse() {
            // given
            User user = createUser(1L);
            given(authService.getCurrentUser()).willReturn(user);
            given(wishListRepository.findAllByUserIdAndWishListTrue(
                    anyLong(), any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());

            // when
            PageResponse<MyUpcomingResponse> result = wishListService.getMyWishList(PageRequest.of(0, 10));

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
        }

        @Test
        @DisplayName("페이지 정보가 그대로 전달된다")
        void paginationIsPassedThrough() {
            // given
            User user = createUser(1L);
            given(authService.getCurrentUser()).willReturn(user);
            given(wishListRepository.findAllByUserIdAndWishListTrue(
                    anyLong(), any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());

            Pageable pageable = PageRequest.of(2, 5);

            // when
            wishListService.getMyWishList(pageable);

            // then
            then(wishListRepository).should().findAllByUserIdAndWishListTrue(
                    anyLong(), eq(pageable), any(LocalDate.class));
        }
    }


    private User createUser(Long id) {
        User user = User.builder()
                .email("user" + id + "@test.com")
                .profileImageUrl("https://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_user" + id)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.AUDIENCE)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
