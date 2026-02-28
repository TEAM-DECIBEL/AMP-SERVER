package com.amp.domain.festival.service.user;

import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.festival.service.audience.AudienceFestivalService;
import com.amp.global.security.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AudienceFestivalService 테스트")
class AudienceFestivalServiceTest {

    @Mock
    private FestivalRepository festivalRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AudienceFestivalService audienceFestivalService;

    @Nested
    @DisplayName("getAllFestivals - LocalDate.now() 파라미터 전달 검증")
    class GetAllFestivals {

        @Test
        @DisplayName("오늘 날짜(LocalDate.now())를 today 파라미터로 전달한다")
        void passesLocalDateNowAsTodayParam() {
            // given
            given(authService.getCurrentUserOrNull()).willReturn(null);
            given(festivalRepository.findActiveFestivals(any(Pageable.class), any(LocalDate.class)))
                    .willReturn(Page.empty());

            Pageable pageable = PageRequest.of(0, 10);

            // when
            audienceFestivalService.getAllFestivals(pageable);

            // then
            ArgumentCaptor<LocalDate> dateCaptor = ArgumentCaptor.forClass(LocalDate.class);
            then(festivalRepository).should().findActiveFestivals(eq(pageable), dateCaptor.capture());

            assertThat(dateCaptor.getValue()).isEqualTo(LocalDate.now());
        }
    }
}
