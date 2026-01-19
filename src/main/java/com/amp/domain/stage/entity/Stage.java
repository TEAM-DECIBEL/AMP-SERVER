package com.amp.domain.stage.entity;

import com.amp.domain.festival.entity.Festival;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "stage")
@SQLRestriction("deleted_at IS NULL")
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

    @Column(name = "location")
    private String location;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Stage(Long id, Festival festival, String title, String location) {
        this.id = id;
        this.festival = festival;
        this.title = title;
        this.location = location;
    }

    public void update(String title, String location) {
        this.title = title;
        this.location = location;
    }
}
