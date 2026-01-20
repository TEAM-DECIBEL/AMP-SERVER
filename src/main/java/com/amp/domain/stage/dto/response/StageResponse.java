package com.amp.domain.stage.dto.response;

import com.amp.domain.stage.entity.Stage;

public record StageResponse(
        Long id,
        String title,
        String location
) {
    public static StageResponse from(Stage stage) {
        return new StageResponse(stage.getId(), stage.getTitle(), stage.getLocation());
    }
}
