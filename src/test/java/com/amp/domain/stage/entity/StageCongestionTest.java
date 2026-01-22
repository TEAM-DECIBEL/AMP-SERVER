package com.amp.domain.stage.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("혼잡도 도메인 및 가중치 계산 테스트")
class StageCongestionTest {

    @Test
    @DisplayName("점수에 따른 혼잡도 레벨 변환 테스트")
    void congestionLevelFromScoreTest() {

        assertThat(CongestionLevel.fromScore(1.0)).isEqualTo(CongestionLevel.SMOOTH);
        assertThat(CongestionLevel.fromScore(2.0)).isEqualTo(CongestionLevel.NORMAL);
        assertThat(CongestionLevel.fromScore(3.0)).isEqualTo(CongestionLevel.CROWDED);
        assertThat(CongestionLevel.fromScore(0)).isEqualTo(CongestionLevel.NONE);
    }

    @Test
    @DisplayName("보고 시점에 따른 가중치 반환 테스트")
    void calculateWeightTest() {
        LocalDateTime now = LocalDateTime.now();

        // 15분 이내: 1.0, 30분 이내: 0.75, 45분 이내: 0.5, 그 외: 0.25
        assertThat(calculateWeight(now.minusMinutes(10), now)).isEqualTo(1.0);
        assertThat(calculateWeight(now.minusMinutes(25), now)).isEqualTo(0.75);
        assertThat(calculateWeight(now.minusMinutes(40), now)).isEqualTo(0.5);
        assertThat(calculateWeight(now.minusMinutes(55), now)).isEqualTo(0.25);
    }

    double calculateWeight(LocalDateTime reportTime, LocalDateTime now) {
        long diff = ChronoUnit.MINUTES.between(reportTime, now);
        if (diff <= 15) return 1.0;
        if (diff <= 30) return 0.75;
        if (diff <= 45) return 0.5;
        return 0.25;
    }
}