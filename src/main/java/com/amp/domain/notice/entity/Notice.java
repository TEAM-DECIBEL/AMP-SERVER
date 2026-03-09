package com.amp.domain.notice.entity;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.user.entity.Organizer;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notice")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_category_id", nullable = false)
    private FestivalCategory festivalCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Organizer organizer;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<NoticeImage> images = new ArrayList<>();

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Notice(Festival festival, FestivalCategory festivalCategory, Organizer organizer,
                  String title, String content, Boolean isPinned) {
        this.festival = festival;
        this.festivalCategory = festivalCategory;
        this.organizer = organizer;
        this.title = title;
        this.content = content;
        this.isPinned = isPinned != null ? isPinned : false;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void update(
            String title,
            String content,
            Boolean isPinned,
            FestivalCategory festivalCategory
    ) {
        this.title = title;
        this.content = content;
        this.isPinned = isPinned;
        this.festivalCategory = festivalCategory;
    }
}
