package com.amp.domain.auth.entity;

import com.amp.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "organizer_registration")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizerRegistration extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "registration_code", nullable = false, length = 20)
    private String registrationCode;

    @Column(nullable = false)
    private boolean verified = false;

    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    private int attemptCount = 0;

    private LocalDateTime lastAttemptAt;

    @Builder
    public OrganizerRegistration(String email, String registrationCode) {
        this.email = email;
        this.registrationCode = registrationCode;
        this.verified = false;
        this.attemptCount = 0;
    }

    public void incrementAttemptCount() {
        this.attemptCount++;
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void markAsVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public boolean isMaxAttemptsExceeded(int maxAttempts) {
        return this.attemptCount >= maxAttempts;
    }
}
