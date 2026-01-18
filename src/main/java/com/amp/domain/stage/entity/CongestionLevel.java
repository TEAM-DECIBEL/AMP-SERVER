package com.amp.domain.stage.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CongestionLevel {
    NONE("입력값이 없습니다.", 0),
    SMOOTH("여유", 1),
    NORMAL("보통", 2),
    CROWDED("혼잡", 3);

    private final String description;
    private final int score;

    public static CongestionLevel fromScore(double score) {
        if (score <= 0) return NONE;
        if (score <= 1.5) return SMOOTH;
        if (score <= 2.5) return NORMAL;
        return CROWDED;
    }
}
