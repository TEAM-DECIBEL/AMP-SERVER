package com.amp.domain.festival.service.organizer;

import com.amp.domain.category.repository.FestivalCategoryRepository;
import com.amp.domain.category.service.FestivalCategoryService;
import com.amp.domain.festival.dto.request.FestivalCreateRequest;
import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.festival.scheduler.FestivalScheduleService;
import com.amp.domain.congestion.repository.StageRepository;
import com.amp.domain.congestion.service.StageService;
import com.amp.global.exception.CustomException;
import com.amp.global.s3.S3Service;
import com.amp.global.security.service.AuthService;
import com.amp.global.support.TestFixtures;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("FestivalService - stages 파싱 및 유효성 검증 테스트")
class FestivalServiceStagesTest {

    @Mock private FestivalRepository festivalRepository;
    @Mock private StageRepository stageRepository;
    @Mock private FestivalScheduleRepository festivalScheduleRepository;
    @Mock private FestivalCategoryRepository festivalCategoryRepository;
    @Mock private FestivalScheduleService scheduleService;
    @Mock private StageService stageService;
    @Mock private FestivalCategoryService categoryService;
    @Mock private AuthService authService;
    @Mock private S3Service s3Service;
    @Spy  private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @InjectMocks
    private FestivalService festivalService;

    private static final String VALID_SCHEDULES =
            "[{\"festivalDate\":\"2026-08-01\",\"festivalTime\":\"18:00\"}]";
    private static final String VALID_CATEGORIES = "[1]";

    @BeforeEach
    void setUp() {
        given(authService.getCurrentUser()).willReturn(
                TestFixtures.organizer("organizer@test.com", "테스트주최자")
        );
    }

    private FestivalCreateRequest requestWith(String stages) {
        MockMultipartFile image = new MockMultipartFile(
                "mainImage", "test.jpg", "image/jpeg", new byte[]{1}
        );
        return new FestivalCreateRequest(
                "테스트 공연", "고양시 일산서구", image,
                VALID_SCHEDULES, stages, VALID_CATEGORIES
        );
    }

    @Nested
    @DisplayName("stages가 비어있는 경우 - STAGES_REQUIRED 예외")
    class EmptyStages {

        @Test
        @DisplayName("stages가 null이면 STAGES_REQUIRED 예외가 발생한다")
        void throwsWhenStagesIsNull() {
            assertThatThrownBy(() -> festivalService.createFestival(requestWith(null)))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(FestivalErrorCode.STAGES_REQUIRED));
        }

        @Test
        @DisplayName("stages가 빈 배열[] 이면 STAGES_REQUIRED 예외가 발생한다")
        void throwsWhenStagesIsEmptyArray() {
            assertThatThrownBy(() -> festivalService.createFestival(requestWith("[]")))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(FestivalErrorCode.STAGES_REQUIRED));
        }
    }

    @Nested
    @DisplayName("stages JSON 형식이 잘못된 경우 - INVALID_STAGE_FORMAT 예외")
    class InvalidStagesFormat {

        @Test
        @DisplayName("stages가 유효하지 않은 JSON 문자열이면 INVALID_STAGE_FORMAT 예외가 발생한다")
        void throwsWhenStagesIsInvalidJson() {
            assertThatThrownBy(() -> festivalService.createFestival(requestWith("not-valid-json")))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(FestivalErrorCode.INVALID_STAGE_FORMAT));
        }

        @Test
        @DisplayName("stages가 배열이 아닌 JSON 객체이면 INVALID_STAGE_FORMAT 예외가 발생한다")
        void throwsWhenStagesIsJsonObject() {
            assertThatThrownBy(() -> festivalService.createFestival(requestWith("{\"title\":\"메인 무대\"}")))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(FestivalErrorCode.INVALID_STAGE_FORMAT));
        }
    }
}
