package com.amp.domain.stage.entity;

import com.amp.domain.festival.entity.Festival;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Stage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stage_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Stage(Festival festival, String title, String location) {
        this.festival = festival;
        this.title = title;
        this.location = location;
    }
}
