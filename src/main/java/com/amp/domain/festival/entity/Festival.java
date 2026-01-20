package com.amp.domain.festival.entity;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.stage.entity.Stage;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FestivalStatus status;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FestivalSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stage> stages = new ArrayList<>();

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FestivalCategory> festivalCategories = new ArrayList<>();

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Organizer> organizers = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Festival(Long id, String title, String mainImageUrl, String location,
                    LocalDate startDate, LocalDate endDate, LocalTime startTime, FestivalStatus status) {
        this.id = id;
        this.title = title;
        this.mainImageUrl = mainImageUrl;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
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

    public void updateInfo(String title, String location) {
        this.title = title;
        this.location = location;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void updateDates(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
}
