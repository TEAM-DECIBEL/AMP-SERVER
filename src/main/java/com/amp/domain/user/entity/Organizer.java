package com.amp.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ORGANIZER")
@SuperBuilder
@NoArgsConstructor
@Getter
public class Organizer extends User {

    @Column(name = "organizer_name", length = 100)
    private String organizerName;

    // 주최자 온보딩 완료
    public void completeOrganizerOnboarding(String organizerName) {
        this.organizerName = organizerName;
        finishOnboarding();
    }
}
