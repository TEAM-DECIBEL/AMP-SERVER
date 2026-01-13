package com.amp.domain.festival.entity;

import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "festival_schedule")
public class FestivalSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "festival_date", nullable = false)
    private LocalDate festivalDate;

    @Column(name = "festival_time", nullable = false)
    private LocalTime festivalTime;

    @Builder
    public FestivalSchedule(Festival festival,
                            LocalDate festivalDate, LocalTime festivalTime) {
        this.festival = festival;
        this.festivalDate = festivalDate;
        this.festivalTime = festivalTime;
    }

    public void update(LocalDate date, LocalTime time) {
        this.festivalDate = date;
        this.festivalTime = time;
    }
}
