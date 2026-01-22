package com.amp.domain.festival.service.user;

import com.amp.domain.festival.dto.response.AudienceFestivalSummaryResponse;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.festival.service.organizer.OrganizerFestivalService;
import com.amp.domain.user.entity.User;
import com.amp.domain.wishList.repository.WishListRepository;
import com.amp.global.common.dto.response.PageResponse;
import com.amp.global.security.service.AuthService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudienceFestivalServiceTest {

    @InjectMocks
    private OrganizerFestivalService organizerFestivalService;

    @InjectMocks
    private AudienceFestivalService audienceFestivalService;

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private WishListRepository wishListRepository;

    @Mock
    private AuthService authService;

    private User mockUser;
    private Festival mockFestival;

    @BeforeEach
    void setUp() {
        mockUser = mock(User.class);
        mockFestival = mock(Festival.class);

        lenient().when(mockFestival.getTitle()).thenReturn("테스트 공연");
        lenient().when(mockFestival.getStartDate()).thenReturn(LocalDate.now());
        lenient().when(mockFestival.getEndDate()).thenReturn(LocalDate.now().plusDays(1));
        lenient().when(mockFestival.getStatus()).thenReturn(FestivalStatus.UPCOMING);
        lenient().when(mockFestival.getLocation()).thenReturn("서울");
    }

    @Test
    @DisplayName("로그인한 사용자가 공연 목록 조회하면 자신의 관람 예정 여부도 표현된다.")
    void getAllFestivals_WithLoggedInUser() {

        Pageable pageable = PageRequest.of(0, 10);

        // 로그인한 유저 객체
        User user = User.builder().id(1L).email("test@test.com").build();

        // 페스티벌 객체 생성
        Festival festival = Festival.builder()
                .id(100L)
                .title("그린 민트 페스티벌")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 2))
                .build();

        Page<Festival> festivalPage = new PageImpl<>(List.of(festival));

        given(authService.getCurrentUserOrNull()).willReturn(user);
        given(festivalRepository.findActiveFestivals(any(Pageable.class))).willReturn(festivalPage);
        given(wishListRepository.findAllFestivalIdsByUserId(user.getId())).willReturn(Set.of(100L));

        // when
        PageResponse<AudienceFestivalSummaryResponse> response = audienceFestivalService.getAllFestivals(pageable);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().wishList()).isTrue();
        verify(wishListRepository).findAllFestivalIdsByUserId(user.getId());
    }

    @Test
    @DisplayName("비로그인 사용자가 공연 목록 조회하면 관람 예정 여부는 다 false이다.")
    void getAllFestivals_WithAnonymousUser() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // 페스티벌 객체 생성
        Festival festival = Festival.builder()
                .id(100L)
                .title("그린 민트 페스티벌")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 1, 2))
                .build();
        Page<Festival> festivalPage = new PageImpl<>(List.of(festival));

        given(authService.getCurrentUserOrNull()).willReturn(null);
        given(festivalRepository.findActiveFestivals(any(Pageable.class))).willReturn(festivalPage);

        // when
        PageResponse<AudienceFestivalSummaryResponse> response = audienceFestivalService.getAllFestivals(pageable);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).wishList()).isFalse();
        verify(wishListRepository, never()).findAllFestivalIdsByUserId(any());
    }
}
