package com.amp.domain.festival.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FestivalStatus {
    UPCOMING("관람 예정"),
    ONGOING("관람 예정"),
    COMPLETED("관람 예정");

    private final String koreanName;
}
