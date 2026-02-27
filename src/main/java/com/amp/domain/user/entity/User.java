package com.amp.domain.user.entity;

import com.amp.global.exception.CustomException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DiscriminatorFormula;

import static com.amp.domain.user.exception.UserErrorCode.USER_TYPE_UNCHANGEABLE;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uq_organizer_name", columnNames = "organizer_name")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("COALESCE(user_type, 'PENDING')")
@DiscriminatorValue("PENDING")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

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
        if (this.registrationStatus == RegistrationStatus.COMPLETED &&
                this.userType != null &&
                this.userType != userType) {
            throw new CustomException(USER_TYPE_UNCHANGEABLE);
        }
        this.userType = userType;
    }

    // 공통 온보딩 완료 처리 (서브클래스에서 호출)
    protected void finishOnboarding() {
        this.registrationStatus = RegistrationStatus.COMPLETED;
        this.isActive = true;
    }

    // 관객 온보딩 완료
    public void completeAudienceOnboarding(String nickname) {
        this.nickname = nickname;
        finishOnboarding();
    }

}
