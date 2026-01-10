package com.amp.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false, name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RegistrationStatus registrationStatus = RegistrationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String organizerName; // 조직명도 nickName 으로 퉁칠까 회의 후 결정

    public void updateExistingUser(String username, String profileImageUrl) {
        this.nickname = username;
        this.profileImageUrl = profileImageUrl;
    }


    public void completeOnboarding(UserType userType, String name) {
        this.userType = userType;
        this.registrationStatus = RegistrationStatus.COMPLETED;

        if (userType == UserType.ORGANIZER) {
            this.organizerName = name;
            this.role = Role.ORGANIZER;
        } else {
            this.nickname = name;
            this.role = Role.USER;
        }
    }


    public boolean isOnboardingCompleted() {
        return this.registrationStatus == RegistrationStatus.COMPLETED;
    }

}
