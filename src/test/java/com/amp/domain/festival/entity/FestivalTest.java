package com.amp.domain.festival.entity;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.entity.FestivalStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FestivalTest {

    @Test
    @DisplayName("공연 시작 전이면 UPCOMING 상태여야 한다")
    void statusShouldBeUpcomingBeforeStartDate() {
        Festival festival = Festival.builder()
                .startDate(LocalDate.of(2026, 1, 20))
                .endDate(LocalDate.of(2026, 1, 22))
                .build();

        // festival.updateStatus(LocalDate.of(2026, 1, 15));

        assertThat(festival.getStatus()).isEqualTo(FestivalStatus.UPCOMING);
    }

    @Test
    @DisplayName("공연 기간 중이면 ONGOING 상태여야 한다")
    void statusShouldBeOngoingDuringFestival() {
        Festival festival = Festival.builder()
                .startDate(LocalDate.of(2026, 1, 20))
                .endDate(LocalDate.of(2026, 1, 22))
                .build();

       // festival.updateStatus(LocalDate.of(2026, 1, 21));

        assertThat(festival.getStatus()).isEqualTo(FestivalStatus.ONGOING);
    }
}
