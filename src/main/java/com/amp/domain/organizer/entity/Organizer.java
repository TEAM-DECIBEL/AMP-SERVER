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
import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "organizer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Festival> festivals = new ArrayList<>();

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
    public Organizer(User user, String organizerName,
                     String contactEmail, String contactPhone,
                     String description) {
        this.user = user;
        this.organizerName = organizerName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.description = description;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void addFestival(Festival festival) {
        this.festivals.add(festival);
        festival.setOrganizer(this);
    }
}
