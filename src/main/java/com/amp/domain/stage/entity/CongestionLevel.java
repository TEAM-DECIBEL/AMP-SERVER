package com.amp.domain.stage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CongestionLevel {
    SMOOTH("여유", 0, 30),
    NORMAL("보통", 31, 70),
    CROWDED("혼잡", 71, 100);

    private final String description;
    private final int minPercent;
    private final int maxPercent;

    public static CongestionLevel fromPercent(int percent) {
        if (percent <= 30) return SMOOTH;
        if (percent <= 70) return NORMAL;
        return CROWDED;
    }
}
