package com.amp.domain.festival.dto.request;

import com.amp.domain.stage.dto.request.StageRequest;
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

@DisplayName("FestivalUpdateRequest - stages 유효성 검증 테스트")
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

    private StageRequest stageWith(String title, String location) throws Exception {
        String json = location != null
                ? String.format("{\"title\":\"%s\",\"location\":\"%s\"}", title, location)
                : String.format("{\"title\":\"%s\"}", title);
        return objectMapper.readValue(json, StageRequest.class);
    }

    private StageRequest stageWithNullTitle() throws Exception {
        return objectMapper.readValue("{}", StageRequest.class);
    }

    private FestivalUpdateRequest requestWith(List<StageRequest> stages) throws Exception {
        return new FestivalUpdateRequest(
                "테스트 공연",
                "고양시 일산서구",
                List.of(schedule()),
                stages,
                List.of(1L)
        );
    }

    private boolean hasStagesViolation(Set<ConstraintViolation<FestivalUpdateRequest>> violations) {
        return violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().startsWith("stages"));
    }

    @Nested
    @DisplayName("stages 리스트가 비어있는 경우 - 검증 실패")
    class InvalidStagesList {

        @Test
        @DisplayName("stages가 null이면 유효성 검증에 실패한다")
        void failsWhenStagesIsNull() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(requestWith(null));

            assertThat(hasStagesViolation(violations)).isTrue();
        }

        @Test
        @DisplayName("stages가 빈 리스트이면 유효성 검증에 실패한다")
        void failsWhenStagesIsEmpty() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(requestWith(List.of()));

            assertThat(hasStagesViolation(violations)).isTrue();
        }

        @Test
        @DisplayName("stages 위반 시 에러 메시지가 올바르다")
        void violationMessageIsCorrect() throws Exception {
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(requestWith(null));

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
            List<StageRequest> stages = List.of(stageWithNullTitle());
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(requestWith(stages));

            assertThat(hasStagesViolation(violations)).isTrue();
        }

        @Test
        @DisplayName("stage의 title이 빈 문자열이면 유효성 검증에 실패한다")
        void failsWhenStageTitleIsBlank() throws Exception {
            StageRequest blankTitle = objectMapper.readValue("{\"title\":\"\"}", StageRequest.class);
            List<StageRequest> stages = List.of(blankTitle);
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(requestWith(stages));

            assertThat(hasStagesViolation(violations)).isTrue();
        }
    }

    @Nested
    @DisplayName("stages가 유효한 경우 - 검증 통과")
    class ValidStages {

        @Test
        @DisplayName("title만 있고 location이 없어도 유효성 검증을 통과한다 (location은 선택)")
        void passesWhenStageHasTitleOnly() throws Exception {
            List<StageRequest> stages = List.of(stageWith("메인 무대", null));
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(requestWith(stages));

            assertThat(hasStagesViolation(violations)).isFalse();
        }

        @Test
        @DisplayName("title과 location이 모두 있으면 유효성 검증을 통과한다")
        void passesWhenStageHasTitleAndLocation() throws Exception {
            List<StageRequest> stages = List.of(stageWith("메인 무대", "A구역"));
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(requestWith(stages));

            assertThat(hasStagesViolation(violations)).isFalse();
        }

        @Test
        @DisplayName("여러 stage가 모두 유효하면 유효성 검증을 통과한다")
        void passesWhenMultipleValidStages() throws Exception {
            List<StageRequest> stages = List.of(
                    stageWith("메인 무대", "A구역"),
                    stageWith("서브 무대", null)
            );
            Set<ConstraintViolation<FestivalUpdateRequest>> violations = validator.validate(requestWith(stages));

            assertThat(hasStagesViolation(violations)).isFalse();
        }
    }
}
