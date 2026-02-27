package com.amp.support;

import com.amp.domain.user.entity.Audience;
import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.Organizer;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static User pendingUser(String email) {
        return User.builder()
                .email(email)
                .profileImageUrl("https://example.com/profile.png")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_" + email)
                .registrationStatus(RegistrationStatus.PENDING)
                .build();
    }

    public static Audience audience(String email, String nickname) {
        return Audience.builder()
                .email(email)
                .profileImageUrl("https://example.com/profile.png")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_" + email)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.AUDIENCE)
                .nickname(nickname)
                .build();
    }

    public static Organizer organizer(String email, String organizerName) {
        return Organizer.builder()
                .email(email)
                .profileImageUrl("https://example.com/profile.png")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_" + email)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.ORGANIZER)
                .organizerName(organizerName)
                .build();
    }

    /**
     * @deprecated Use {@link #audience(String, String)} or {@link #organizer(String, String)} instead
     */
    @Deprecated
    public static User user(String email, String nickname) {
        return organizer(email, nickname);
    }
}
