package com.amp.domain.user.entity;

import com.amp.global.exception.CustomException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.amp.domain.user.exception.UserErrorCode.USER_TYPE_UNCHANGEABLE;

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

    @Column(unique = true)
    private String nickname;

    @Column(nullable = false, name = "profile_image_url")
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false, name = "provider_id")
    private String providerId;

    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RegistrationStatus registrationStatus = RegistrationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    public void updateExistingUser(String username, String profileImageUrl) {
        this.nickname = username;
        this.profileImageUrl = profileImageUrl;
    }

    // 온보딩 중 UserType 임시 설정
    public void updateUserType(UserType userType) {
        if (this.userType != null) {
            throw new CustomException(USER_TYPE_UNCHANGEABLE);
        }
        this.userType = userType;
    }

    // 관객 온보딩 완료
    public void completeAudienceOnboarding(String nickname) {
        this.nickname = nickname;
        this.registrationStatus = RegistrationStatus.COMPLETED;
        this.isActive = true;
    }

    // 주최자 온보딩 완료 (닉네임만 업데이트, Organizer 엔티티는 별도 생성)
    public void completeOrganizerOnboarding(String nickname) {
        this.nickname = nickname;
        this.registrationStatus = RegistrationStatus.COMPLETED;
        this.isActive = true;
    }

}