package com.amp.domain.festival.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FestivalCreateRequest - stages 유효성 검증 테스트")
class FestivalCreateRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private FestivalCreateRequest requestWith(String stages) {
        return new FestivalCreateRequest(
                "테스트 공연",
                "고양시 일산서구",
                new MockMultipartFile("image", "test.jpg", "image/jpeg", new byte[]{1}),
                "[{\"festivalDate\":\"2026-08-01\",\"festivalTime\":\"18:00\"}]",
                stages,
                "[1]"
        );
    }

    private boolean hasStagesViolation(Set<ConstraintViolation<FestivalCreateRequest>> violations) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("stages"));
    }

    @Nested
    @DisplayName("stages가 누락된 경우 - 검증 실패")
    class InvalidStages {

        @Test
        @DisplayName("stages가 null이면 유효성 검증에 실패한다")
        void failsWhenStagesIsNull() {
            Set<ConstraintViolation<FestivalCreateRequest>> violations = validator.validate(requestWith(null));

            assertThat(hasStagesViolation(violations)).isTrue();
        }

        @Test
        @DisplayName("stages가 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenStagesIsEmpty() {
            Set<ConstraintViolation<FestivalCreateRequest>> violations = validator.validate(requestWith(""));

            assertThat(hasStagesViolation(violations)).isTrue();
        }

        @Test
        @DisplayName("stages가 공백 문자열이면 유효성 검증에 실패한다")
        void failsWhenStagesIsWhitespace() {
            Set<ConstraintViolation<FestivalCreateRequest>> violations = validator.validate(requestWith("   "));

            assertThat(hasStagesViolation(violations)).isTrue();
        }

        @Test
        @DisplayName("stages 위반 시 에러 메시지가 올바르다")
        void violationMessageIsCorrect() {
            Set<ConstraintViolation<FestivalCreateRequest>> violations = validator.validate(requestWith(null));

            String message = violations.stream()
                    .filter(v -> v.getPropertyPath().toString().equals("stages"))
                    .map(ConstraintViolation::getMessage)
                    .findFirst()
                    .orElse("");

            assertThat(message).isEqualTo("1개 이상의 무대/부스 정보는 필수입니다.");
        }
    }

    @Nested
    @DisplayName("stages가 유효한 경우 - 검증 통과")
    class ValidStages {

        @Test
        @DisplayName("stages에 title만 있어도 유효성 검증을 통과한다 (location은 선택)")
        void passesWhenStagesHasTitleOnly() {
            String stages = "[{\"title\":\"메인 무대\"}]";
            Set<ConstraintViolation<FestivalCreateRequest>> violations = validator.validate(requestWith(stages));

            assertThat(hasStagesViolation(violations)).isFalse();
        }

        @Test
        @DisplayName("stages에 title과 location이 모두 있으면 유효성 검증을 통과한다")
        void passesWhenStagesHasTitleAndLocation() {
            String stages = "[{\"title\":\"메인 무대\",\"location\":\"A구역\"}]";
            Set<ConstraintViolation<FestivalCreateRequest>> violations = validator.validate(requestWith(stages));

            assertThat(hasStagesViolation(violations)).isFalse();
        }

        @Test
        @DisplayName("stages에 여러 무대가 있어도 유효성 검증을 통과한다")
        void passesWhenStagesHasMultipleEntries() {
            String stages = "[{\"title\":\"메인 무대\",\"location\":\"A구역\"},{\"title\":\"서브 무대\"}]";
            Set<ConstraintViolation<FestivalCreateRequest>> violations = validator.validate(requestWith(stages));

            assertThat(hasStagesViolation(violations)).isFalse();
        }
    }
}
