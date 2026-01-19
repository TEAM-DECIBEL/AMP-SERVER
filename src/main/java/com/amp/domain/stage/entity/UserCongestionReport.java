package com.amp.domain.stage.entity;

import com.amp.domain.user.entity.User;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "user_congestion_report",
        indexes = {
                @Index(name = "idx_reported_at", columnList = "reported_at"),
                @Index(name = "idx_stage_reported_at", columnList = "stage_id, reported_at")
        }
)
public class UserCongestionReport extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private Stage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "reported_level", nullable = false)
    private CongestionLevel reportedLevel;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Builder
    public UserCongestionReport(User user, Stage stage,
                                CongestionLevel reportedLevel,
                                LocalDateTime reportedAt) {
        this.user = user;
        this.stage = stage;
        this.reportedLevel = reportedLevel;
        this.reportedAt = reportedAt != null ? reportedAt : LocalDateTime.now();
    }
}
