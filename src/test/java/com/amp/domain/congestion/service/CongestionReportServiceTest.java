package com.amp.domain.congestion.service;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalSchedule;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.congestion.entity.CongestionLevel;
import com.amp.domain.congestion.entity.Stage;
import com.amp.domain.congestion.exception.StageErrorCode;
import com.amp.domain.congestion.repository.StageRepository;
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
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CongestionReportServiceTest {

    @InjectMocks private CongestionReportService service;

    @Mock private StageRepository stageRepository;
    @Mock private FestivalScheduleRepository festivalScheduleRepository;
    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private AuthService authService;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private ListOperations<String, String> listOps;
    @Mock private Festival mockFestival;

    private static final Long STAGE_ID = 1L;
    private static final Long FESTIVAL_ID = 1L;

    private User testUser;
    private Stage testStage;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .profileImageUrl("https://example.com/img.png")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_123")
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.AUDIENCE)
                .build();

        testStage = Stage.builder()
                .id(STAGE_ID)
                .festival(mockFestival)
                .title("테스트 무대")
                .build();
    }

    private FestivalSchedule schedule(LocalDate date, LocalTime time) {
        return FestivalSchedule.builder()
                .festival(mockFestival)
                .festivalDate(date)
                .festivalTime(time)
                .build();
    }

   // 시간 검증까지 가는데 필요한 mock
    private void givenStageReady() {
        given(authService.getCurrentUser()).willReturn(testUser);
        given(stageRepository.findById(STAGE_ID)).willReturn(Optional.of(testStage));
        given(mockFestival.getId()).willReturn(FESTIVAL_ID);
    }

    // Redis까지 가는데 필요한 mock
    private void givenRedisReady() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(redisTemplate.opsForList()).willReturn(listOps);
        given(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).willReturn(true);
    }

    @Nested
    @DisplayName("입력값 검증")
    class InputValidation {

        @Test
        @DisplayName("level이 NONE이면 INVALID_CONGESTION_LEVEL을 던진다")
        void rejectWhenCongestionLevelIsNone() {
            assertThatThrownBy(() -> service.reportCongestion(STAGE_ID, CongestionLevel.NONE))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.INVALID_CONGESTION_LEVEL);
        }

        @Test
        @DisplayName("level이 null이면 INVALID_CONGESTION_LEVEL을 던진다")
        void rejectWhenCongestionLevelIsNull() {
            assertThatThrownBy(() -> service.reportCongestion(STAGE_ID, null))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.INVALID_CONGESTION_LEVEL);
        }

        @Test
        @DisplayName("존재하지 않는 stageId면 STAGE_NOT_FOUND를 던진다")
        void rejectWhenStageNotFound() {
            given(authService.getCurrentUser()).willReturn(testUser);
            given(stageRepository.findById(STAGE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.reportCongestion(STAGE_ID, CongestionLevel.CROWDED))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.STAGE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("시간 검증 - 오늘 공연")
    class TodaySchedule {

        private final LocalDate TODAY = LocalDate.of(2025, 6, 15);

        @BeforeEach
        void setup() {
            given(mockFestival.getId()).willReturn(FESTIVAL_ID);
        }

        @Test
        @DisplayName("공연 8시간 이내면 성공하고 Redis에 저장된다")
        void succeedWhenReportWithinEightHoursBeforeStart() {
            // 공연 18:00, enableTime 10:00, now 14:00 → 통과
            LocalDateTime now = LocalDateTime.of(TODAY, LocalTime.of(14, 0));
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of(schedule(TODAY, LocalTime.of(18, 0))));

            assertDoesNotThrow(() -> service.validateReportTime(testStage, now));
        }

        @Test
        @DisplayName("공연 8시간 이전이면 TOO_EARLY_TO_REPORT를 던진다")
        void failWhenReportMoreThanEightHoursBeforeStart() {
            // 공연 18:00, enableTime 10:00, now 09:00 → 차단
            LocalDateTime now = LocalDateTime.of(TODAY, LocalTime.of(9, 0));
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of(schedule(TODAY, LocalTime.of(18, 0))));

            assertThatThrownBy(() -> service.validateReportTime(testStage, now))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.TOO_EARLY_TO_REPORT);
        }

        @Test
        @DisplayName("공연 시작 정각 8시간 전이면 통과한다 (경계값)")
        void succeedWhenReportExactlyEightHoursBeforeStart() {
            // 공연 18:00, enableTime 10:00, now 10:00 → 통과
            LocalDateTime now = LocalDateTime.of(TODAY, LocalTime.of(10, 0));
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of(schedule(TODAY, LocalTime.of(18, 0))));

            assertDoesNotThrow(() -> service.validateReportTime(testStage, now));
        }
    }

    @Nested
    @DisplayName("시간 검증 - 내일 새벽 공연")
    class TomorrowSchedule {

        private final LocalDate TODAY    = LocalDate.of(2025, 6, 15);
        private final LocalDate TOMORROW = LocalDate.of(2025, 6, 16);

        @BeforeEach
        void setup() {
            given(mockFestival.getId()).willReturn(FESTIVAL_ID);
        }

        @Test
        @DisplayName("내일 새벽 공연이고 8시간 이내면 성공한다")
        void succeedWhenReportWithinEightHoursForEarlyMorningShow() {
            // 내일 02:00 → enableTime = 오늘 18:00, now = 오늘 20:00 → 통과
            LocalDateTime now = LocalDateTime.of(TODAY, LocalTime.of(20, 0));
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of(schedule(TOMORROW, LocalTime.of(2, 0))));

            assertDoesNotThrow(() -> service.validateReportTime(testStage, now));
        }

        @Test
        @DisplayName("내일 새벽 공연이지만 8시간 이전이면 TOO_EARLY_TO_REPORT를 던진다")
        void failWhenReportTooEarlyForEarlyMorningShow() {
            // 내일 02:00 → enableTime = 오늘 18:00, now = 오늘 15:00 → 차단
            LocalDateTime now = LocalDateTime.of(TODAY, LocalTime.of(15, 0));
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of(schedule(TOMORROW, LocalTime.of(2, 0))));

            assertThatThrownBy(() -> service.validateReportTime(testStage, now))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.TOO_EARLY_TO_REPORT);
        }
    }

    @Nested
    @DisplayName("시간 검증 - 기타")
    class EdgeCases {

        private final LocalDate TODAY = LocalDate.of(2025, 6, 15);

        @BeforeEach
        void setup() {
            given(mockFestival.getId()).willReturn(FESTIVAL_ID);
        }

        @Test
        @DisplayName("공연이 종료됐으면 FESTIVAL_ENDED를 던진다")
        void failWhenFestivalHasEnded() {
            LocalDateTime now = LocalDateTime.of(TODAY, LocalTime.of(14, 0));
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of(schedule(TODAY.minusDays(1), LocalTime.of(18, 0))));

            assertThatThrownBy(() -> service.validateReportTime(testStage, now))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.FESTIVAL_ENDED);
        }

        @Test
        @DisplayName("다회차 공연 사이 쉬는 날이면 NO_SCHEDULE_TODAY를 던진다")
        void failWhenNoScheduleOnReportingDay() {
            LocalDateTime now = LocalDateTime.of(TODAY, LocalTime.of(14, 0));
            // 어제, 모레 공연 있고 오늘은 없음
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of(
                            schedule(TODAY.minusDays(1), LocalTime.of(18, 0)),
                            schedule(TODAY.plusDays(2), LocalTime.of(18, 0))
                    ));

            assertThatThrownBy(() -> service.validateReportTime(testStage, now))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.NO_SCHEDULE_TODAY);
        }

        @Test
        @DisplayName("스케줄이 없으면 NO_SCHEDULE_FOUND를 던진다")
        void failWhenNoScheduleFound() {
            LocalDateTime now = LocalDateTime.of(TODAY, LocalTime.of(14, 0));
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of());

            assertThatThrownBy(() -> service.validateReportTime(testStage, now))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.NO_SCHEDULE_FOUND);
        }

        @Test
        @DisplayName("공연 이틀 전이면 TOO_EARLY_TO_REPORT를 던진다")
        void failWhenReportingTooManyDaysInAdvance() {
            LocalDateTime now = LocalDateTime.of(TODAY, LocalTime.of(14, 0));
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of(schedule(TODAY.plusDays(2), LocalTime.of(18, 0))));

            assertThatThrownBy(() -> service.validateReportTime(testStage, now))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.TOO_EARLY_TO_REPORT);
        }
    }

    @Nested
    @DisplayName("Redis 중복 방지")
    class RedisDeduplication {

        private final LocalDate TODAY = LocalDate.now();

        @BeforeEach
        void setup() {
            givenStageReady();
            given(festivalScheduleRepository.findByFestivalIdOrderByFestivalDate(FESTIVAL_ID))
                    .willReturn(List.of(schedule(TODAY, LocalTime.of(0, 0))));
            given(redisTemplate.opsForValue()).willReturn(valueOps);
        }

        @Test
        @DisplayName("15분 이내 중복 제보면 ALREADY_REPORTED_RECENTLY를 던진다")
        void failWhenDuplicateReportWithinCoolDown() {
            given(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                    .willReturn(false);

            assertThatThrownBy(() -> service.reportCongestion(STAGE_ID, CongestionLevel.CROWDED))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(StageErrorCode.ALREADY_REPORTED_RECENTLY);
        }

        @Test
        @DisplayName("정상 제보면 Redis stage:reports 키에 저장된다")
        void saveToRedisWhenReportIsSuccessful() {
            given(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                    .willReturn(true);
            given(redisTemplate.opsForList()).willReturn(listOps);

            service.reportCongestion(STAGE_ID, CongestionLevel.CROWDED);

            verify(listOps).rightPush(eq("stage:reports:" + STAGE_ID), anyString());
            verify(redisTemplate).expire(eq("stage:reports:" + STAGE_ID), anyLong(), any());
        }
    }
}
