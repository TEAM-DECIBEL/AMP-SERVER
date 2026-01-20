package com.amp.domain.festival.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class FestivalScheduleTest {

    @Test
    @DisplayName("FestivalSchedule 엔티티 생성 테스트")
    void createFestivalSchedule() {
        // given
        Festival mockFestival = Mockito.mock(Festival.class);
        LocalDate date = LocalDate.of(2026, 1, 20);
        LocalTime time = LocalTime.of(14, 0);

        // when
        FestivalSchedule schedule = FestivalSchedule.builder()
                .festival(mockFestival)
                .festivalDate(date)
                .festivalTime(time)
                .build();

        // then
        assertThat(schedule.getFestival()).isEqualTo(mockFestival);
        assertThat(schedule.getFestivalDate()).isEqualTo(date);
        assertThat(schedule.getFestivalTime()).isEqualTo(time);
    }

    @Test
    @DisplayName("일정 수정(update) 테스트")
    void updateSchedule() {
        // given
        Festival mockFestival = Mockito.mock(Festival.class);
        FestivalSchedule schedule = FestivalSchedule.builder()
                .festival(mockFestival)
                .festivalDate(LocalDate.now())
                .festivalTime(LocalTime.now())
                .build();

        LocalDate newDate = LocalDate.now().plusDays(1);
        LocalTime newTime = LocalTime.of(19, 30);

        // when
        schedule.update(newDate, newTime);

        // then
        assertThat(schedule.getFestivalDate()).isEqualTo(newDate);
        assertThat(schedule.getFestivalTime()).isEqualTo(newTime);
    }
}