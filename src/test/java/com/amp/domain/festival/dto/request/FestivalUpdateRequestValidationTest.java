package com.amp.domain.festival.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FestivalUpdateRequest - 유효성 검증 테스트")
class FestivalUpdateRequestValidationTest {

    private static Validator validator;

    private static final String VALID_SCHEDULES = "[{\"festivalDate\":\"2026-08-01\",\"festivalTime\":\"18:00\"}]";
    private static final String VALID_STAGES = "[{\"title\":\"메인 무대\"}]";
    private static final String VALID_CATEGORY_IDS = "[1]";

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private FestivalUpdateRequest requestWith(
            String title,
            String location,
            String schedules,
            String stages,
            String activeCategoryIds
    ) {
        return new FestivalUpdateRequest(title, location, null, schedules, stages, activeCategoryIds);
    }

    private boolean hasViolationOn(Set<ConstraintViolation<FestivalUpdateRequest>> violations, String field) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().startsWith(field));
    }

    @Nested
    @DisplayName("title 검증")
    class TitleValidation {

        @Test
        @DisplayName("title이 null이면 유효성 검증에 실패한다")
        void failsWhenTitleIsNull() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith(null, "고양시 일산서구", VALID_SCHEDULES, VALID_STAGES, VALID_CATEGORY_IDS)
            );
            assertThat(hasViolationOn(violations, "title")).isTrue();
        }

        @Test
        @DisplayName("title이 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenTitleIsBlank() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("", "고양시 일산서구", VALID_SCHEDULES, VALID_STAGES, VALID_CATEGORY_IDS)
            );
            assertThat(hasViolationOn(violations, "title")).isTrue();
        }
    }

    @Nested
    @DisplayName("location 검증")
    class LocationValidation {

        @Test
        @DisplayName("location이 null이면 유효성 검증에 실패한다")
        void failsWhenLocationIsNull() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", null, VALID_SCHEDULES, VALID_STAGES, VALID_CATEGORY_IDS)
            );
            assertThat(hasViolationOn(violations, "location")).isTrue();
        }

        @Test
        @DisplayName("location이 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenLocationIsBlank() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "", VALID_SCHEDULES, VALID_STAGES, VALID_CATEGORY_IDS)
            );
            assertThat(hasViolationOn(violations, "location")).isTrue();
        }
    }

    @Nested
    @DisplayName("schedules 검증")
    class SchedulesValidation {

        @Test
        @DisplayName("schedules가 null이면 유효성 검증에 실패한다")
        void failsWhenSchedulesIsNull() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", null, VALID_STAGES, VALID_CATEGORY_IDS)
            );
            assertThat(hasViolationOn(violations, "schedules")).isTrue();
        }

        @Test
        @DisplayName("schedules가 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenSchedulesIsBlank() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", "", VALID_STAGES, VALID_CATEGORY_IDS)
            );
            assertThat(hasViolationOn(violations, "schedules")).isTrue();
        }
    }

    @Nested
    @DisplayName("stages 검증")
    class StagesValidation {

        @Test
        @DisplayName("stages가 null이면 유효성 검증에 실패한다")
        void failsWhenStagesIsNull() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", VALID_SCHEDULES, null, VALID_CATEGORY_IDS)
            );
            assertThat(hasViolationOn(violations, "stages")).isTrue();
        }

        @Test
        @DisplayName("stages가 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenStagesIsBlank() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", VALID_SCHEDULES, "", VALID_CATEGORY_IDS)
            );
            assertThat(hasViolationOn(violations, "stages")).isTrue();
        }

        @Test
        @DisplayName("stages 위반 시 에러 메시지가 올바르다")
        void violationMessageIsCorrect() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", VALID_SCHEDULES, null, VALID_CATEGORY_IDS)
            );
            String message = violations.stream()
                    .filter(v -> v.getPropertyPath().toString().equals("stages"))
                    .map(ConstraintViolation::getMessage)
                    .findFirst()
                    .orElse("");
            assertThat(message).isEqualTo("1개 이상의 무대/부스 정보는 필수입니다.");
        }
    }

    @Nested
    @DisplayName("activeCategoryIds 검증")
    class ActiveCategoryIdsValidation {

        @Test
        @DisplayName("activeCategoryIds가 null이면 유효성 검증에 실패한다")
        void failsWhenCategoryIdsIsNull() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", VALID_SCHEDULES, VALID_STAGES, null)
            );
            assertThat(hasViolationOn(violations, "activeCategoryIds")).isTrue();
        }

        @Test
        @DisplayName("activeCategoryIds가 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenCategoryIdsIsBlank() {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", VALID_SCHEDULES, VALID_STAGES, "")
            );
            assertThat(hasViolationOn(violations, "activeCategoryIds")).isTrue();
        }
    }
}
