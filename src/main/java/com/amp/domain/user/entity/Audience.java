package com.amp.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("AUDIENCE")
@SuperBuilder
@NoArgsConstructor
@Getter
public class Audience extends User {

    @Column(name = "nickname", length = 12)
    private String nickname;

    /**
     * 관객 온보딩 완료
     */
    public void completeAudienceOnboarding(String nickname) {
        this.nickname = nickname;
        finishOnboarding();
    }
}
