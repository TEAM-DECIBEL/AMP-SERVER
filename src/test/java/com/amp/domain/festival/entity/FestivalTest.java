package com.amp.domain.festival.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class FestivalTest {

    @Test
    @DisplayName("Festival 엔티티 생성 테스트")
    void createFestival() {
        // given
        String title = "둥글게";
        String location = "서울 올림픽공원";
        LocalDate startDate = LocalDate.now().plusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(10);

        // when
        Festival festival = Festival.builder()
                .title(title)
                .mainImageUrl("https://image.com/jazz.png")
                .location(location)
                .startDate(startDate)
                .endDate(endDate)
                .startTime(LocalTime.of(18, 0))
                .status(FestivalStatus.UPCOMING)
                .build();

        // then
        assertThat(festival.getTitle()).isEqualTo(title);
        assertThat(festival.getLocation()).isEqualTo(location);
        assertThat(festival.getStartDate()).isEqualTo(startDate);
    }

    @Nested
    @DisplayName("상태 업데이트(updateStatus) 테스트")
    class UpdateStatusTest {

        @Test
        @DisplayName("시작 날짜가 미래라면 UPCOMING 상태가 된다")
        void statusUpcoming() {
            // given
            Festival festival = createFestivalWithDates(
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(5)
            );

            // when
            festival.updateStatus();

            // then
            assertThat(festival.getStatus()).isEqualTo(FestivalStatus.UPCOMING);
        }

        @Test
        @DisplayName("현재 날짜가 시작과 종료 사이라면 ONGOING 상태가 된다")
        void statusOngoing() {
            // given
            Festival festival = createFestivalWithDates(
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(1)
            );

            // when
            festival.updateStatus();

            // then
            assertThat(festival.getStatus()).isEqualTo(FestivalStatus.ONGOING);
        }

        @Test
        @DisplayName("종료 날짜가 과거라면 COMPLETED 상태가 된다")
        void statusCompleted() {
            // given
            Festival festival = createFestivalWithDates(
                    LocalDate.now().minusDays(5),
                    LocalDate.now().minusDays(1)
            );

            // when
            festival.updateStatus();

            // then
            assertThat(festival.getStatus()).isEqualTo(FestivalStatus.COMPLETED);
        }
    }

    @Test
    @DisplayName("정보 수정(updateInfo) 테스트")
    void updateInfo() {
        // given
        Festival festival = createFestivalWithDates(LocalDate.now(), LocalDate.now());
        String newTitle = "수정된 페스티벌 이름";
        String newLocation = "수정된 장소";

        // when
        festival.updateInfo(newTitle, newLocation);

        // then
        assertThat(festival.getTitle()).isEqualTo(newTitle);
        assertThat(festival.getLocation()).isEqualTo(newLocation);
    }

    @Test
    @DisplayName("삭제(delete) 호출 시 deletedAt에 현재 시간이 기록된다")
    void deleteTest() {
        // given
        Festival festival = createFestivalWithDates(LocalDate.now(), LocalDate.now());

        // when
        festival.delete();

        // then
        assertThat(festival.getDeletedAt()).isNotNull();
        assertThat(festival.getDeletedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    private Festival createFestivalWithDates(LocalDate start, LocalDate end) {
        return Festival.builder()
                .title("테스트 페스티벌")
                .mainImageUrl("url")
                .location("장소")
                .startDate(start)
                .endDate(end)
                .startTime(LocalTime.now())
                .status(FestivalStatus.UPCOMING)
                .build();
    }
}