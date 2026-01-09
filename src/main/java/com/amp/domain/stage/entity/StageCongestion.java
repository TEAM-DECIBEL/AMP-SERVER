package com.amp.domain.stage.entity;


import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stage_congestion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StageCongestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "congestion_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "congestion_level", nullable = false, length = 20)
    private CongestionLevel congestionLevel;

    @Column(name = "current_count")
    private Integer currentCount;

    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    @Builder
    public StageCongestion(Stage stage, CongestionLevel congestionLevel,
                           Integer currentCount, LocalDateTime measuredAt) {
        this.stage = stage;
        this.congestionLevel = congestionLevel;
        this.currentCount = currentCount;
        this.measuredAt = measuredAt != null ? measuredAt : LocalDateTime.now();
    }
}
