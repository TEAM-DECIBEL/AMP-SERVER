package com.amp.support;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;

import java.time.LocalDate;
import java.time.LocalTime;

public final class TestFixtures {

    private TestFixtures() {}

    public static User user(String email, String nickname) {
        return User.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .provider(AuthProvider.GOOGLE)       // ✅ 너희 enum에 맞게
                .providerId("google_" + email)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.ORGANIZER)        // 필요 없으면 제거
                .isActive(true)
                .build();
    }

    public static Organizer organizer(User user) {
        return Organizer.builder()
                .user(user)
                .organizerName("ORG_" + user.getNickname())
                .contactEmail(user.getEmail())
                .contactPhone("010-0000-0000")
                .description("test organizer")
                .build();
    }

    public static Festival festival(Organizer organizer, String title, FestivalStatus status) {
        return Festival.builder()
                .title(title)
                .mainImageUrl("https://example.com/main.png")
                .location("Seoul")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(18, 0))
                .status(status)
                .organizer(organizer)
                .build();
    }

    public static Festival ongoingFestival(Organizer organizer, String title) {
        return Festival.builder()
                .title(title)
                .mainImageUrl("https://example.com/main.png")
                .location("Seoul")
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(18, 0))
                .status(FestivalStatus.ONGOING)
                .organizer(organizer)
                .build();
    }

    public static Festival upcomingFestival(Organizer organizer, String title) {
        return festival(organizer, title, FestivalStatus.UPCOMING);
    }

    public static Festival completedFestival(Organizer organizer, String title) {
        return Festival.builder()
                .title(title)
                .mainImageUrl("https://example.com/main.png")
                .location("Seoul")
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(18, 0))
                .status(FestivalStatus.COMPLETED)
                .organizer(organizer)
                .build();
    }
}
