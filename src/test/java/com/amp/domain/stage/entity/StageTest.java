package com.amp.domain.stage.entity;

import com.amp.domain.festival.entity.Festival;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StageTest {

    @Test
    @DisplayName("Stage 엔티티 생성 테스트")
    void createStageEntity() {
        // given
        Festival festival = Festival.builder()
                .id(1L)
                .title("테스트 축제")
                .build();
        String title = "메인 스테이지";
        String location = "운동장 A구역";

        // when
        Stage stage = Stage.builder()
                .festival(festival)
                .title(title)
                .location(location)
                .build();

        // then
        assertThat(stage.getFestival()).isEqualTo(festival);
        assertThat(stage.getTitle()).isEqualTo(title);
        assertThat(stage.getLocation()).isEqualTo(location);

    }

    @Test
    @DisplayName("Stage 엔티티 정보 수정 테스트")
    void updateStage() {
        // given
        Stage stage = Stage.builder()
                .title("기존 제목")
                .location("기존 위치")
                .build();

        String newTitle = "수정된 제목";
        String newLocation = "수정된 위치";

        // when
        stage.update(newTitle, newLocation);
        // then
        assertThat(stage.getTitle()).isEqualTo(newTitle);
        assertThat(stage.getLocation()).isEqualTo(newLocation);

    }

}
