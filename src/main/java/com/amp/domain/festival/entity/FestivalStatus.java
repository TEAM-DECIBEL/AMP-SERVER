package com.amp.domain.festival.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FestivalStatus {
    UPCOMING("진행 예정"),
    ONGOING("진행 중"),
    COMPLETED("진행 완료");

    private final String koreanName;
}
