package com.amp.domain.announcement.entity;

import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_saved_announcement",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_announcement",
                columnNames = {"user_id", "announcement_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSavedAnnouncement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saved_announcement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "announcement_id", nullable = false)
    private Announcement announcement;

    @Builder
    public UserSavedAnnouncement(User user, Announcement announcement) {
        this.user = user;
        this.announcement = announcement;
    }

}
