package com.amp.support;

import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;


public final class TestFixtures {

    private TestFixtures() {}

    public static User user(String email, String nickname) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_" + email)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.ORGANIZER)
                .isActive(true)
                .build();
    }

}
