package com.amp.domain.organizer.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrganizerRepositoryTest {

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("유저의 특정 상태 페스티벌 개수를 정상적으로 카운트한다")
    void countFestivalsByUserIdAndStatus_Success() {
        // given
        User user = createUser("test@email.com");
        em.persist(user);

        Festival upcomingFestival1 = createFestival("Festival 1", FestivalStatus.UPCOMING);
        Festival upcomingFestival2 = createFestival("Festival 2", FestivalStatus.UPCOMING);
        Festival ongoingFestival = createFestival("Festival 3", FestivalStatus.ONGOING);
        em.persist(upcomingFestival1);
        em.persist(upcomingFestival2);
        em.persist(ongoingFestival);

        Organizer organizer1 = createOrganizer(user, upcomingFestival1);
        Organizer organizer2 = createOrganizer(user, upcomingFestival2);
        Organizer organizer3 = createOrganizer(user, ongoingFestival);
        em.persist(organizer1);
        em.persist(organizer2);
        em.persist(organizer3);

        em.flush();
        em.clear();

        // when
        Long upcomingCount = organizerRepository.countFestivalsByUserIdAndStatus(
                user.getId(), FestivalStatus.UPCOMING
        );
        Long ongoingCount = organizerRepository.countFestivalsByUserIdAndStatus(
                user.getId(), FestivalStatus.ONGOING
        );
        Long completedCount = organizerRepository.countFestivalsByUserIdAndStatus(
                user.getId(), FestivalStatus.COMPLETED
        );

        // then
        assertThat(upcomingCount).isEqualTo(2L);
        assertThat(ongoingCount).isEqualTo(1L);
        assertThat(completedCount).isEqualTo(0L);
    }

    @Test
    @DisplayName("festival이 null인 Organizer는 카운트에서 제외된다")
    void countFestivalsByUserIdAndStatus_ExcludesNullFestival() {
        // given
        User user = createUser("test@email.com");
        em.persist(user);

        Festival festival = createFestival("Festival 1", FestivalStatus.UPCOMING);
        em.persist(festival);

        Organizer organizerWithFestival = createOrganizer(user, festival);
        Organizer organizerWithoutFestival = createOrganizer(user, null); // festival이 null
        em.persist(organizerWithFestival);
        em.persist(organizerWithoutFestival);

        em.flush();
        em.clear();

        // when
        Long count = organizerRepository.countFestivalsByUserIdAndStatus(
                user.getId(), FestivalStatus.UPCOMING
        );

        // then
        assertThat(count).isEqualTo(1L); // null festival은 제외
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .nickname("testUser")
                .profileImageUrl("https://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("google123")
                .isActive(true)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.ORGANIZER)
                .build();
    }

    private Festival createFestival(String title, FestivalStatus status) {
        return Festival.builder()
                .title(title)
                .mainImageUrl("https://example.com/festival.jpg")
                .location("서울시 강남구")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(12))
                .startTime(LocalTime.of(18, 0))
                .status(status)
                .build();
    }

    private Organizer createOrganizer(User user, Festival festival) {
        return Organizer.builder()
                .user(user)
                .festival(festival)
                .organizerName("Test Organizer")
                .contactEmail("organizer@email.com")
                .contactPhone("010-1234-5678")
                .description("Test description")
                .build();
    }
}