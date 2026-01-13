package com.amp.domain.festival.entity;

import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "festival")
@Getter
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Festival extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "main_image_url", nullable = false, length = 500)
    private String mainImageUrl;

    @Column(nullable = false, length = 255)
    private String location;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FestivalStatus status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Festival(String title, String mainImageUrl, String location,
                    LocalDate startDate, LocalDate endDate, FestivalStatus status) {
        this.title = title;
        this.mainImageUrl = mainImageUrl;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public void updateStatus() {
        LocalDate now = LocalDate.now();

        if (now.isBefore(this.startDate)) {
            this.status = FestivalStatus.UPCOMING;
        } else if (now.isAfter(this.endDate)) {
            this.status = FestivalStatus.COMPLETED;
        } else {
            this.status = FestivalStatus.ONGOING;
        }
    }
}
