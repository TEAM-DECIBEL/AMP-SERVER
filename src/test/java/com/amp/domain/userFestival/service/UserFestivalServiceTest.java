package com.amp.domain.userFestival.service;

import com.amp.domain.festival.common.entity.Festival;
import com.amp.domain.festival.common.entity.FestivalStatus;
import com.amp.domain.userFestival.dto.response.RecentFestivalResponse;
import com.amp.domain.userFestival.repository.UserFestivalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFestivalService 테스트")
class UserFestivalServiceTest {

    @Mock
    private UserFestivalRepository userFestivalRepository;

    @InjectMocks
    private UserFestivalService userFestivalService;

    @Test
    @DisplayName("최근 공연 조회 성공 - 공연이 1개 있는 경우")
    void getRecentFestival_Success_SingleFestival() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.plusDays(15);

        Festival festival = createFestival(1L, "Grand Mint Festival", startDate);

        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of(festival));

        // when
        Optional<RecentFestivalResponse> result = userFestivalService.getRecentFestival(userId);

        // then
        assertThat(result).isPresent(); // Optional 안에 데이터가 있는지 확인
        assertThat(result.get().getFestivalId()).isEqualTo(1L);
        assertThat(result.get().getDDay()).isEqualTo(15L);
    }

    @Test
    @DisplayName("최근 공연 조회 - 공연이 없는 경우 Optional.empty() 반환")
    void getRecentFestival_ReturnEmpty_WhenNoFestival() {
        // given
        Long userId = 1L;
        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of()); // 빈 리스트 반환

        // when
        Optional<RecentFestivalResponse> result = userFestivalService.getRecentFestival(userId);

        // then
        assertThat(result).isEmpty(); // 예외가 터지지 않고 비어있는지 확인
        assertThat(result).isEqualTo(Optional.empty());

        then(userFestivalRepository).should(times(1))
                .findUpcomingWishlistFestivals(eq(userId), any(LocalDate.class));
    }

    @Test
    @DisplayName("최근 공연 조회 성공 - 여러 공연 중 가장 임박한 공연(첫 번째) 반환")
    void getRecentFestival_Success_MultipleFestivals() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        Festival festival1 = createFestival(1L, "Nearest Festival", today.plusDays(5));
        Festival festival2 = createFestival(2L, "Far Festival", today.plusDays(20));

        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of(festival1, festival2));

        // when
        Optional<RecentFestivalResponse> result = userFestivalService.getRecentFestival(userId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getFestivalId()).isEqualTo(1L); // 첫 번째 요소인지 확인
        assertThat(result.get().getTitle()).isEqualTo("Nearest Festival");
    }

    // 테스트용 Festival 객체 생성 헬퍼 메서드
    private Festival createFestival(Long id, String title, LocalDate startDate) {
        Festival festival = Festival.builder()
                .title(title)
                .mainImageUrl("https://example.com/image.jpg")
                .location("테스트 장소")
                .startDate(startDate)
                .endDate(startDate.plusDays(1))
                .status(FestivalStatus.UPCOMING)
                .build();
        ReflectionTestUtils.setField(festival, "id", id);
        return festival;
    }

}