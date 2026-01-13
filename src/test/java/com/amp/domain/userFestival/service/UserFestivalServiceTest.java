package com.amp.domain.userFestival.service;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.userFestival.dto.RecentFestivalResponse;
import com.amp.domain.userFestival.repository.UserFestivalRepository;
import com.amp.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static com.amp.global.common.CommonErrorCode.NO_RECENT_FESTIVAL;
import static org.assertj.core.api.Assertions.*;
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
        LocalDate endDate = today.plusDays(16);

        Festival festival = createFestival(
                1L,
                "Grand Mint Festival",
                "https://example.com/image.jpg",
                "올림픽공원 일대",
                startDate,
                endDate
        );

        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of(festival));

        // when
        RecentFestivalResponse result = userFestivalService.getRecentFestival(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFestivalId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Grand Mint Festival");
        assertThat(result.getMainImageUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(result.getLocation()).isEqualTo("올림픽공원 일대");
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        assertThat(result.getDDay()).isEqualTo(15L);

        // verify
        then(userFestivalRepository).should(times(1))
                .findUpcomingWishlistFestivals(eq(userId), any(LocalDate.class));
    }

    @Test
    @DisplayName("최근 공연 조회 성공 - 여러 공연 중 가장 임박한 공연 반환")
    void getRecentFestival_Success_MultipleFestivals_ReturnNearest() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.now();

        Festival festival1 = createFestival(
                1L,
                "Festival 1",
                "https://example.com/image1.jpg",
                "장소 1",
                today.plusDays(10),
                today.plusDays(11)
        );

        Festival festival2 = createFestival(
                2L,
                "Festival 2",
                "https://example.com/image2.jpg",
                "장소 2",
                today.plusDays(20),
                today.plusDays(21)
        );

        Festival festival3 = createFestival(
                3L,
                "Festival 3",
                "https://example.com/image3.jpg",
                "장소 3",
                today.plusDays(30),
                today.plusDays(31)
        );

        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of(festival1, festival2, festival3));

        // when
        RecentFestivalResponse result = userFestivalService.getRecentFestival(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getFestivalId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Festival 1");
        assertThat(result.getDDay()).isEqualTo(10L);
    }

    @Test
    @DisplayName("최근 공연 조회 실패 - 공연이 없는 경우 CustomException 발생")
    void getRecentFestival_Fail_NoFestival_ThrowsException() {
        // given
        Long userId = 1L;
        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> userFestivalService.getRecentFestival(userId))
                .isInstanceOf(CustomException.class)
                .hasMessage(NO_RECENT_FESTIVAL.getMsg())
                .extracting("errorCode")
                .isEqualTo(NO_RECENT_FESTIVAL);

        then(userFestivalRepository).should(times(1))
                .findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class));
    }

    @Test
    @DisplayName("D-Day 계산 테스트 - 미래 공연 (양수)")
    void getRecentFestival_DDay_FutureFestival() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.plusDays(30);

        Festival festival = createFestival(
                1L,
                "Future Festival",
                "https://example.com/image.jpg",
                "미래 장소",
                startDate,
                startDate.plusDays(1)
        );

        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of(festival));

        // when
        RecentFestivalResponse result = userFestivalService.getRecentFestival(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDDay()).isEqualTo(30L);
    }

    @Test
    @DisplayName("D-Day 계산 테스트 - 진행 중인 공연 (음수)")
    void getRecentFestival_DDay_OngoingFestival() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(5);
        LocalDate endDate = today.plusDays(2);

        Festival festival = createFestival(
                1L,
                "Ongoing Festival",
                "https://example.com/image.jpg",
                "진행중 장소",
                startDate,
                endDate
        );

        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of(festival));

        // when
        RecentFestivalResponse result = userFestivalService.getRecentFestival(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getDDay()).isEqualTo(-5L);
    }

    @Test
    @DisplayName("하루짜리 공연 - startDate와 endDate가 같은 경우")
    void getRecentFestival_OneDayFestival() {
        // given
        Long userId = 1L;
        LocalDate today = LocalDate.now();
        LocalDate festivalDate = today.plusDays(10);

        Festival festival = createFestival(
                1L,
                "One Day Festival",
                "https://example.com/image.jpg",
                "하루 공연장",
                festivalDate,
                festivalDate
        );

        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of(festival));

        // when
        RecentFestivalResponse result = userFestivalService.getRecentFestival(userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(result.getEndDate());
        assertThat(result.getDDay()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Repository 메서드 호출 확인 - 올바른 파라미터 전달")
    void getRecentFestival_VerifyRepositoryCall() {
        // given
        Long userId = 999L;
        Festival festival = createFestival(
                1L,
                "Test Festival",
                "https://example.com/image.jpg",
                "테스트 장소",
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(11)
        );

        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of(festival));

        // when
        userFestivalService.getRecentFestival(userId);

        // then
        then(userFestivalRepository).should(times(1))
                .findUpcomingWishlistFestivals(
                        eq(userId),
                        argThat(date -> date.equals(LocalDate.now()))
                );
    }

    @Test
    @DisplayName("예외 발생 시 ErrorCode 확인")
    void getRecentFestival_Exception_ErrorCode() {
        // given
        Long userId = 1L;
        given(userFestivalRepository.findUpcomingWishlistFestivals(anyLong(), any(LocalDate.class)))
                .willReturn(List.of());

        // when & then
        assertThatThrownBy(() -> userFestivalService.getRecentFestival(userId))
                .isInstanceOf(CustomException.class)
                .satisfies(exception -> {
                    CustomException customException = (CustomException) exception;
                    assertThat(customException.getErrorCode().getCode()).isEqualTo("UFE_404_001");
                    assertThat(customException.getErrorCode().getMsg()).isEqualTo("최근에 보는 공연이 없습니다.");
                    assertThat(customException.getErrorCode().getHttpStatus().value()).isEqualTo(404);
                });
    }

    private Festival createFestival(
            Long festivalId,
            String title,
            String mainImageUrl,
            String location,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Festival festival = Festival.builder()
                .title(title)
                .mainImageUrl(mainImageUrl)
                .location(location)
                .startDate(startDate)
                .endDate(endDate)
                .status(FestivalStatus.UPCOMING)
                .build();

        // Reflection으로 id 설정 (테스트 환경에서만 사용)
        ReflectionTestUtils.setField(festival, "id", festivalId);

        return festival;
    }

}