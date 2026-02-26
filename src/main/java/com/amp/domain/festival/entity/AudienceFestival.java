package com.amp.domain.festival.entity;

import com.amp.domain.user.entity.Audience;
import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "user_festival")
public class AudienceFestival extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_festival_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Audience audience;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @Column(name = "wish_list", nullable = false)
    private Boolean wishList = false;

    @Builder
    public AudienceFestival(Audience audience, Festival festival, Boolean wishList) {
        this.audience = audience;
        this.festival = festival;
        this.wishList = wishList != null ? wishList : false;
    }

    public void updateWishList(boolean wishList) {
        this.wishList = wishList;
    }
}
