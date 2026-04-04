package com.amp.domain.congestion.service;

import com.amp.domain.congestion.dto.response.FestivalCongestionResponse;
import com.amp.domain.congestion.entity.CongestionLevel;
import com.amp.domain.congestion.entity.Stage;
import com.amp.domain.congestion.entity.StageCongestion;
import com.amp.domain.congestion.repository.StageCongestionRepository;
import com.amp.domain.congestion.repository.StageRepository;
import com.amp.domain.festival.entity.FestivalSchedule;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.global.exception.CustomException;
import com.amp.global.security.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CongestionQueryServiceTest {

    @InjectMocks
    private CongestionQueryService congestionQueryService;

    @Mock private AuthService authService;
    @Mock private StageRepository stageRepository;
    @Mock private FestivalScheduleRepository festivalScheduleRepository;
    @Mock private StageCongestionRepository stageCongestionRepository;
    @Mock private FestivalRepository festivalRepository;

    private static final Long FESTIVAL_ID = 1L;
    private static final Long STAGE_ID = 1L;
    private static final Pageable PAGEABLE =
            PageRequest.of(0, 10);

    private Stage testStage;
    private StageCongestion testCongestion;
    private User audienceUser;
    private User organizerUser;

    @BeforeEach
    void setUp() {
        testStage = Stage.builder()
                .id(STAGE_ID)
                .title("테스트 무대")
                .location("A구역")
                .build();

        testCongestion = StageCongestion.builder()
                .stage(testStage)
                .congestionLevel(CongestionLevel.CROWDED)
                .measuredAt(LocalDateTime.now())
                .build();

        // 테스트용 관객
        audienceUser = User.builder()
                .email("audience@test.com")
                .profileImageUrl("https://example.com/img.png")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_audience")
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.AUDIENCE)
                .build();

        // 테스트용 주최사
        organizerUser = User.builder()
                .email("organizer@test.com")
                .profileImageUrl("https://example.com/img.png")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_organizer")
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.ORGANIZER)
                .build();
    }

    private void givenFestivalExists() {
        given(festivalRepository.existsById(FESTIVAL_ID)).willReturn(true);
    }

    private void givenStageWithCongestion() {
        Page<Stage> stagePage = new PageImpl<>(List.of(testStage));
        given(stageRepository.findByFestivalId(eq(FESTIVAL_ID), any(Pageable.class))).willReturn(stagePage);
        given(stageCongestionRepository.findLatestByStageIds(anyList())).willReturn(List.of(testCongestion));
    }

    // 스케줄 시간이 00:00 인 경우
    // 지난 날 16시부터
    private void givenScheduleWindowActive() {
        FestivalSchedule schedule = FestivalSchedule.builder()
                .festivalDate(LocalDate.now())
                .festivalTime(LocalTime.MIDNIGHT)
                .build();
        given(festivalScheduleRepository.findByFestivalIdAndFestivalDate(eq(FESTIVAL_ID), any(LocalDate.class)))
                .willReturn(Optional.of(schedule));
    }

    // 오늘 내일 스케줄 없음
    private void givenScheduleWindowInactive() {
        given(festivalScheduleRepository.findByFestivalIdAndFestivalDate(eq(FESTIVAL_ID), any(LocalDate.class)))
                .willReturn(Optional.empty());
    }

    @Nested
    @DisplayName("isInputAvailable 플래그 검증")
    class InputAvailableTest {

        @Test
        @DisplayName("스케줄 활성 + 관객 → 혼잡도 입력 가능")
        void audienceWithActiveWindow_inputAvailable() {
            givenFestivalExists();
            givenStageWithCongestion();
            givenScheduleWindowActive();
            given(authService.getCurrentUserOrNull()).willReturn(audienceUser);

            FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE);

            assertThat(response.isInputAvailable()).isTrue();
        }

        @Test
        @DisplayName("스케줄 활성 + 주최사 → 혼잡도 입력 불가 (주최사는 제보 대상 아님)")
        void organizerWithActiveWindow_inputNotAvailable() {
            givenFestivalExists();
            givenStageWithCongestion();
            givenScheduleWindowActive();
            given(authService.getCurrentUserOrNull()).willReturn(organizerUser);

            FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE);

            assertThat(response.isInputAvailable()).isFalse();
        }

        @Test
        @DisplayName("스케줄 활성 + 비로그인 → 혼잡도 입력 불가")
        void anonymousWithActiveWindow_inputNotAvailable() {
            givenFestivalExists();
            givenStageWithCongestion();
            givenScheduleWindowActive();
            given(authService.getCurrentUserOrNull()).willReturn(null);

            FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE);

            assertThat(response.isInputAvailable()).isFalse();
        }

        @Test
        @DisplayName("스케줄 비활성 + 관객 → 혼잡도 입력 불가")
        void audienceWithInactiveWindow_inputNotAvailable() {
            givenFestivalExists();
            givenStageWithCongestion();
            givenScheduleWindowInactive();
            given(authService.getCurrentUserOrNull()).willReturn(audienceUser);

            FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE);

            assertThat(response.isInputAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("혼잡도 공개 여부 검증 (isViewAvailable = isScheduleWindowActive)")
    class ViewAvailableTest {

        @Test
        @DisplayName("스케줄 활성 + 관객 → 혼잡도 실제값 표시")
        void audienceWithActiveWindow_showsActualCongestion() {
            givenFestivalExists();
            givenStageWithCongestion();
            givenScheduleWindowActive();
            given(authService.getCurrentUserOrNull()).willReturn(audienceUser);

            FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE);

            assertThat(response.stages().get(0).congestionLevel()).isEqualTo(CongestionLevel.CROWDED);
        }

        @Test
        @DisplayName("스케줄 활성 + 주최사 → 혼잡도 실제값 표시")
        void organizerWithActiveWindow_showsActualCongestion() {
            givenFestivalExists();
            givenStageWithCongestion();
            givenScheduleWindowActive();
            given(authService.getCurrentUserOrNull()).willReturn(organizerUser);

            FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE);

            assertThat(response.stages().get(0).congestionLevel()).isEqualTo(CongestionLevel.CROWDED);
        }

        @Test
        @DisplayName("스케줄 비활성 + 관객 → 혼잡도 NONE (조정중) 표시")
        void audienceWithInactiveWindow_showsNoneCongestion() {
            givenFestivalExists();
            givenStageWithCongestion();
            givenScheduleWindowInactive();
            given(authService.getCurrentUserOrNull()).willReturn(audienceUser);

            FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE);

            assertThat(response.stages().get(0).congestionLevel()).isEqualTo(CongestionLevel.NONE);
        }

        @Test
        @DisplayName("스케줄 비활성 + 주최사 → 혼잡도 NONE (조정중) 표시")
        void organizerWithInactiveWindow_showsNoneCongestion() {
            givenFestivalExists();
            givenStageWithCongestion();
            givenScheduleWindowInactive();
            given(authService.getCurrentUserOrNull()).willReturn(organizerUser);

            FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE);

            assertThat(response.stages().get(0).congestionLevel()).isEqualTo(CongestionLevel.NONE);
        }

        @Test
        @DisplayName("스케줄 비활성 + 비로그인 → 혼잡도 NONE 표시")
        void anonymousWithInactiveWindow_showsNoneCongestion() {
            givenFestivalExists();
            givenStageWithCongestion();
            givenScheduleWindowInactive();
            given(authService.getCurrentUserOrNull()).willReturn(null);

            FestivalCongestionResponse response = congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE);

            assertThat(response.stages().get(0).congestionLevel()).isEqualTo(CongestionLevel.NONE);
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionTest {

        @Test
        @DisplayName("존재하지 않는 festivalId → FESTIVAL_NOT_FOUND 예외")
        void festivalNotFound_throwsException() {
            given(festivalRepository.existsById(FESTIVAL_ID)).willReturn(false);

            assertThatThrownBy(() -> congestionQueryService.getFestivalCongestion(FESTIVAL_ID, PAGEABLE))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(FestivalErrorCode.FESTIVAL_NOT_FOUND);
        }
    }
}