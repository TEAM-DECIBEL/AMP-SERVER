package com.amp.domain.festival.service.organizer;

import com.amp.domain.festival.dto.response.OrganizerActiveFestivalPageResponse;
import com.amp.domain.festival.dto.response.OrganizerFestivalListResponse;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.user.entity.User;
import com.amp.global.common.dto.PageResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OrganizerFestivalServiceTest {

    @InjectMocks
    private OrganizerFestivalService organizerFestivalService;

    @Mock
    private FestivalRepository festivalRepository;

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
    @DisplayName("주최자가 등록한 모든 공연 목록을 조회하고 DTO로 변환한다")
    void getMyFestivalsSuccess() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Festival> festivalPage = new PageImpl<>(List.of(mockFestival), pageable, 1);

        given(authService.getCurrentUser()).willReturn(mockUser);
        given(festivalRepository.findAllByMyUser(eq(mockUser), any(Pageable.class))).willReturn(festivalPage);

        // when
        PageResponse<OrganizerFestivalListResponse> result = organizerFestivalService.getMyFestivals(pageable);

        // then
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("테스트 공연");
        assertThat(result.content().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("진행중 및 예정 공연 개수와 목록을 함께 반환한다")
    void getActiveFestivalsSuccess() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Festival> activePage = new PageImpl<>(List.of(mockFestival), pageable, 1);

        lenient().when(mockFestival.getStatus()).thenReturn(FestivalStatus.ONGOING);

        given(authService.getCurrentUser()).willReturn(mockUser);
        given(festivalRepository.countByOrganizerAndStatus(any(User.class), eq(FestivalStatus.ONGOING))).willReturn(1L);
        given(festivalRepository.countByOrganizerAndStatus(any(User.class), eq(FestivalStatus.UPCOMING))).willReturn(1L);
        given(festivalRepository.findActiveFestivalsByUser(any(User.class), anyList(), any(Pageable.class)))
                .willReturn(activePage);

        // when
        OrganizerActiveFestivalPageResponse result = organizerFestivalService.getActiveFestivals(pageable);

        // then
        assertThat(result.ongoingFestivals().size()).isEqualTo(1L); // 이제 1이 나옵니다!
        assertThat(result.upcomingFestivals().size()).isEqualTo(0L); // 상태를 바꿨으니 예정 공연은 0이 됩니다.
    }
}