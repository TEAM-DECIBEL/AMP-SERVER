package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.global.support.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AudienceFestivalSummaryResponse - status 반환 테스트")
class AudienceFestivalSummaryResponseTest {

    private Festival festivalWithStatus(FestivalStatus status) {
        return Festival.builder()
                .title("테스트 공연")
                .mainImageUrl("https://example.com/image.jpg")
                .location("고양시 일산서구")
                .startDate(LocalDate.of(2026, 3, 3))
                .endDate(LocalDate.of(2026, 3, 3))
                .startTime(LocalTime.of(18, 0))
                .status(status)
                .organizer(TestFixtures.organizer("organizer@test.com", "테스트주최자"))
                .build();
    }

    @Test
    @DisplayName("UPCOMING 상태 공연은 '관람 예정'을 반환한다")
    void returnsExpectedStatusForUpcomingFestival() {
        AudienceFestivalSummaryResponse response =
                AudienceFestivalSummaryResponse.from(festivalWithStatus(FestivalStatus.UPCOMING), false);

        assertThat(response.status()).isEqualTo("관람 예정");
    }

    @Test
    @DisplayName("ONGOING 상태 공연도 '관람 예정'을 반환한다")
    void returnsExpectedStatusForOngoingFestival() {
        AudienceFestivalSummaryResponse response =
                AudienceFestivalSummaryResponse.from(festivalWithStatus(FestivalStatus.ONGOING), false);

        assertThat(response.status()).isEqualTo("관람 예정");
    }
}
