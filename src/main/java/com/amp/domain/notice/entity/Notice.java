package com.amp.domain.notice.entity;

import com.amp.domain.category.entity.FestivalCategory;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.user.entity.User;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcement")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "announcement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_category_id", nullable = false)
    private FestivalCategory festivalCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Notice(Festival festival, FestivalCategory festivalCategory, User user,
                  String title, String content, String imageUrl, Boolean isPinned) {
        this.festival = festival;
        this.festivalCategory = festivalCategory;
        this.user = user;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.isPinned = isPinned != null ? isPinned : false;
    }

}