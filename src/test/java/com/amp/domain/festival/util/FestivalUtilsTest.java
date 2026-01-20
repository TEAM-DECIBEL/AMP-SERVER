package com.amp.domain.festival.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class FestivalUtilsTest {

    @Test
    @DisplayName("공연 기간 포맷팅 테스트")
    void formatPeriodTest() {
        // given
        LocalDate start = LocalDate.of(2026, 1, 21);
        LocalDate end = LocalDate.of(2026, 1, 23);

        // when & then
        // 1. 시작일과 종료일이 다른 경우
        assertThat(FestivalUtils.formatPeriod(start, end)).isEqualTo("2026. 01. 21 - 2026. 01. 23");

        // 2. 시작일과 종료일이 같은 경우
        assertThat(FestivalUtils.formatPeriod(start, start)).isEqualTo("2026. 01. 21");
    }

    @Test
    @DisplayName("오늘 날짜 기준 D-Day 계산 검증 (오늘: 2026-01-21)")
    void calculateDDayTest() {
        // LocalDate.now()를 2026-01-21로 고정
        try (MockedStatic<LocalDate> mockedLocalDate = mockStatic(LocalDate.class)) {
            mockedLocalDate.when(LocalDate::now).thenReturn(LocalDate.of(2026, 1, 21));

            // Case 1: 시작일이 오늘인 경우 (D-0이 나와야 함)
            Long dDayToday = FestivalUtils.calculateDDay(
                    LocalDate.of(2026, 1, 21),
                    LocalDate.of(2026, 1, 23)
            );
            assertThat(dDayToday).isEqualTo(0L);

            // Case 2: 시작일이 미래인 경우 (D-5)
            Long dDayFuture = FestivalUtils.calculateDDay(
                    LocalDate.of(2026, 1, 26),
                    LocalDate.of(2026, 1, 30)
            );
            assertThat(dDayFuture).isEqualTo(5L);

            // Case 3: 공연 기간 중인 경우 (D-0)
            Long dDayOngoing = FestivalUtils.calculateDDay(
                    LocalDate.of(2026, 1, 20),
                    LocalDate.of(2026, 1, 22)
            );
            assertThat(dDayOngoing).isEqualTo(0L);

            // Case 4: 공연이 이미 종료된 경우 (종료 후 1일 경과)
            Long dDayPast = FestivalUtils.calculateDDay(
                    LocalDate.of(2026, 1, 15),
                    LocalDate.of(2026, 1, 20)
            );
            assertThat(dDayPast).isEqualTo(1L);
        }
    }
}