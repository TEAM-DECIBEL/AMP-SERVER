package com.amp.domain.festival.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class FestivalTest {

    private static final Logger log = LoggerFactory.getLogger(FestivalTest.class);

    @Test
    @DisplayName("공연 시작 전이면 UPCOMING 상태여야 한다")
    void statusShouldBeUpcomingBeforeStartDate() {
        Festival festival = Festival.builder()
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .build();

        festival.updateStatus();

        assertThat(festival.getStatus()).isEqualTo(FestivalStatus.UPCOMING);
    }

    @Test
    @DisplayName("공연 기간 중이면 ONGOING 상태여야 한다")
    void statusShouldBeOngoingDuringFestival() {
        Festival festival = Festival.builder()
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build();

        festival.updateStatus();

        assertThat(festival.getStatus()).isEqualTo(FestivalStatus.ONGOING);
    }
}
