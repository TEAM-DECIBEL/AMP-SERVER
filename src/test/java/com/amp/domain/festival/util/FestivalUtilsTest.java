package com.amp.domain.festival.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
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

}