package com.amp.domain.festival.dto.request;

import com.amp.domain.congestion.dto.request.StageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FestivalUpdateRequest - 유효성 검증 테스트")
class FestivalUpdateRequestValidationTest {

    private static Validator validator;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws Exception {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private ScheduleRequest schedule() throws Exception {
        return objectMapper.readValue(
                "{\"festivalDate\":\"2026-08-01\",\"festivalTime\":\"18:00\"}",
                ScheduleRequest.class
        );
    }

    private StageRequest validStage() throws Exception {
        return objectMapper.readValue("{\"title\":\"메인 무대\"}", StageRequest.class);
    }

    private StageRequest stageWith(String title, String location) throws Exception {
        String json = location != null
                ? String.format("{\"title\":\"%s\",\"location\":\"%s\"}", title, location)
                : String.format("{\"title\":\"%s\"}", title);
        return objectMapper.readValue(json, StageRequest.class);
    }

    private FestivalUpdateRequest requestWith(
            String title,
            String location,
            List<ScheduleRequest> schedules,
            List<StageRequest> stages,
            List<Long> activeCategoryIds
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
        void failsWhenTitleIsNull() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith(null, "고양시 일산서구", List.of(schedule()), List.of(validStage()), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "title")).isTrue();
        }

        @Test
        @DisplayName("title이 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenTitleIsBlank() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("", "고양시 일산서구", List.of(schedule()), List.of(validStage()), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "title")).isTrue();
        }
    }

    @Nested
    @DisplayName("location 검증")
    class LocationValidation {

        @Test
        @DisplayName("location이 null이면 유효성 검증에 실패한다")
        void failsWhenLocationIsNull() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", null, List.of(schedule()), List.of(validStage()), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "location")).isTrue();
        }

        @Test
        @DisplayName("location이 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenLocationIsBlank() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "", List.of(schedule()), List.of(validStage()), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "location")).isTrue();
        }
    }

    @Nested
    @DisplayName("schedules 리스트 검증")
    class SchedulesValidation {

        @Test
        @DisplayName("schedules가 null이면 유효성 검증에 실패한다")
        void failsWhenSchedulesIsNull() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", null, List.of(validStage()), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "schedules")).isTrue();
        }

        @Test
        @DisplayName("schedules가 빈 리스트이면 유효성 검증에 실패한다")
        void failsWhenSchedulesIsEmpty() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(), List.of(validStage()), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "schedules")).isTrue();
        }
    }

    @Nested
    @DisplayName("schedule 내부 필드 검증 - @Valid 캐스케이드")
    class ScheduleFieldValidation {

        @Test
        @DisplayName("festivalDate가 null이면 유효성 검증에 실패한다")
        void failsWhenFestivalDateIsNull() throws Exception {
            ScheduleRequest noDate = objectMapper.readValue(
                    "{\"festivalTime\":\"18:00\"}", ScheduleRequest.class
            );
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(noDate), List.of(validStage()), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "schedules")).isTrue();
        }

        @Test
        @DisplayName("festivalTime이 null이면 유효성 검증에 실패한다")
        void failsWhenFestivalTimeIsNull() throws Exception {
            ScheduleRequest noTime = objectMapper.readValue(
                    "{\"festivalDate\":\"2026-08-01\"}", ScheduleRequest.class
            );
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(noTime), List.of(validStage()), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "schedules")).isTrue();
        }
    }

    @Nested
    @DisplayName("stages 리스트 검증")
    class InvalidStagesList {

        @Test
        @DisplayName("stages가 null이면 유효성 검증에 실패한다")
        void failsWhenStagesIsNull() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(schedule()), null, List.of(1L))
            );
            assertThat(hasViolationOn(violations, "stages")).isTrue();
        }

        @Test
        @DisplayName("stages가 빈 리스트이면 유효성 검증에 실패한다")
        void failsWhenStagesIsEmpty() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(schedule()), List.of(), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "stages")).isTrue();
        }

        @Test
        @DisplayName("stages 위반 시 에러 메시지가 올바르다")
        void violationMessageIsCorrect() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(schedule()), null, List.of(1L))
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
    @DisplayName("stage 내부 필드 검증 - @Valid 캐스케이드")
    class StageFieldValidation {

        @Test
        @DisplayName("stage의 title이 null이면 유효성 검증에 실패한다")
        void failsWhenStageTitleIsNull() throws Exception {
            StageRequest nullTitle = objectMapper.readValue("{}", StageRequest.class);
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(schedule()), List.of(nullTitle), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "stages")).isTrue();
        }

        @Test
        @DisplayName("stage의 title이 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenStageTitleIsBlank() throws Exception {
            StageRequest blankTitle = objectMapper.readValue("{\"title\":\"\"}", StageRequest.class);
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(schedule()), List.of(blankTitle), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "stages")).isTrue();
        }

        @Test
        @DisplayName("title만 있고 location이 없어도 유효성 검증을 통과한다 (location은 선택)")
        void passesWhenStageHasTitleOnly() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(schedule()), List.of(stageWith("메인 무대", null)), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "stages")).isFalse();
        }

        @Test
        @DisplayName("title과 location이 모두 있으면 유효성 검증을 통과한다")
        void passesWhenStageHasTitleAndLocation() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(schedule()), List.of(stageWith("메인 무대", "A구역")), List.of(1L))
            );
            assertThat(hasViolationOn(violations, "stages")).isFalse();
        }
    }

    @Nested
    @DisplayName("activeCategoryIds 검증")
    class ActiveCategoryIdsValidation {

        @Test
        @DisplayName("activeCategoryIds가 null이면 유효성 검증에 실패한다")
        void failsWhenCategoryIdsIsNull() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(schedule()), List.of(validStage()), null)
            );
            assertThat(hasViolationOn(violations, "activeCategoryIds")).isTrue();
        }

        @Test
        @DisplayName("activeCategoryIds가 빈 리스트이면 유효성 검증에 실패한다")
        void failsWhenCategoryIdsIsEmpty() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(
                    requestWith("테스트 공연", "고양시 일산서구", List.of(schedule()), List.of(validStage()), List.of())
            );
            assertThat(hasViolationOn(violations, "activeCategoryIds")).isTrue();
        }
    }
}
