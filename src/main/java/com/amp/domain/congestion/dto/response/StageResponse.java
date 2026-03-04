package com.amp.domain.congestion.dto.response;

import com.amp.domain.congestion.entity.Stage;

public record StageResponse(
        Long id,
        String title,
        String location
) {
    public static StageResponse from(Stage stage) {
        return new StageResponse(stage.getId(), stage.getTitle(), stage.getLocation());
    }
}
