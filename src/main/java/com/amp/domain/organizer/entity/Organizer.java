package com.amp.domain.organizer.entity;

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
@Table(name = "organizer")
@Getter
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Organizer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organizer_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = true)
    private Festival festival;

    @Column(name = "organizer_name", nullable = false, length = 100)
    private String organizerName;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Organizer(User user, Festival festival, String organizerName,
                     String contactEmail, String contactPhone,
                     String description) {
        this.user = user;
        this.festival = festival;
        this.organizerName = organizerName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.description = description;
    }

}
