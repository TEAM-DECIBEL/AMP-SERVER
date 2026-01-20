package com.amp.domain.notification.entity;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.user.entity.User;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "alarm",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "festival_category_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alarm_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_category_id", nullable = false)
    private FestivalCategory festivalCategory;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Builder
    public Alarm(User user, FestivalCategory festivalCategory) {
        this.user = user;
        this.festivalCategory = festivalCategory;
    }

}
