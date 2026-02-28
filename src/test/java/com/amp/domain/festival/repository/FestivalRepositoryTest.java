package com.amp.domain.festival.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.user.entity.*;
import com.amp.global.config.JpaAuditConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;NON_KEYWORDS=VALUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("FestivalRepository 테스트")
class FestivalRepositoryTest {

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private TestEntityManager em;

    private Audience audience;
    private Organizer organizer;

    @BeforeEach
    void setUp() {
        organizer = createOrganizer("organizer@test.com");
        em.persist(organizer);

        audience = createAudience("audience@test.com");
        em.persist(audience);

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("findActiveFestivals - :today 파라미터 검증")
    class FindActiveFestivals {

        @Test
        @DisplayName("endDate가 today 이후인 페스티벌은 조회된다")
        void endDateAfterTodayIncluded() {
            // given
            Festival festival = createFestival("진행 예정 페스티벌",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(festival);
            em.flush();
            em.clear();

            // when
            Page<Festival> result = festivalRepository.findActiveFestivals(
                    PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("진행 예정 페스티벌");
        }

        @Test
        @DisplayName("endDate가 today인 페스티벌도 조회된다 (endDate >= today 경계값)")
        void endDateEqualsTodayIncluded() {
            // given
            Festival festival = createFestival("오늘 종료 페스티벌",
                    LocalDate.now().minusDays(1), LocalDate.now());
            em.persist(festival);
            em.flush();
            em.clear();

            // when
            Page<Festival> result = festivalRepository.findActiveFestivals(
                    PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("오늘 종료 페스티벌");
        }

        @Test
        @DisplayName("endDate가 today보다 이전인 페스티벌은 조회되지 않는다")
        void endDateBeforeTodayExcluded() {
            // given
            Festival endedFestival = createFestival("종료된 페스티벌",
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
            em.persist(endedFestival);
            em.flush();
            em.clear();

            // when
            Page<Festival> result = festivalRepository.findActiveFestivals(
                    PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("soft delete된 페스티벌은 조회되지 않는다")
        void softDeletedFestivalExcluded() {
            // given
            Festival festival = createFestival("삭제된 페스티벌",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(festival);
            em.flush();

            festival.delete();
            em.flush();
            em.clear();

            // when
            Page<Festival> result = festivalRepository.findActiveFestivals(
                    PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("today 파라미터로 과거 날짜를 전달하면 종료된 페스티벌도 조회된다")
        void pastDateParamIncludesEndedFestivals() {
            // given
            Festival pastFestival = createFestival("지난 페스티벌",
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
            em.persist(pastFestival);
            em.flush();
            em.clear();

            // when - today를 1주일 전으로 전달
            Page<Festival> result = festivalRepository.findActiveFestivals(
                    PageRequest.of(0, 10), LocalDate.now().minusWeeks(1));

            // then
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findActiveFestivals - 정렬 검증")
    class Sorting {

        @Test
        @DisplayName("startDate ASC, startTime ASC, title ASC 순으로 정렬된다")
        void sortedByStartDateAscStartTimeAscTitleAsc() {
            // given
            Festival festivalC = Festival.builder()
                    .title("C페스티벌")
                    .mainImageUrl("img.jpg")
                    .location("Seoul")
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(3))
                    .startTime(LocalTime.of(18, 0))
                    .status(FestivalStatus.UPCOMING)
                    .organizer(organizer)
                    .build();

            Festival festivalA = Festival.builder()
                    .title("A페스티벌")
                    .mainImageUrl("img.jpg")
                    .location("Seoul")
                    .startDate(LocalDate.now().plusDays(1))
                    .endDate(LocalDate.now().plusDays(3))
                    .startTime(LocalTime.of(18, 0))
                    .status(FestivalStatus.UPCOMING)
                    .organizer(organizer)
                    .build();

            Festival festivalEarlier = Festival.builder()
                    .title("이른 시작 페스티벌")
                    .mainImageUrl("img.jpg")
                    .location("Seoul")
                    .startDate(LocalDate.now()) // startDate가 더 이름
                    .endDate(LocalDate.now().plusDays(3))
                    .startTime(LocalTime.of(12, 0))
                    .status(FestivalStatus.ONGOING)
                    .organizer(organizer)
                    .build();

            em.persist(festivalC);
            em.persist(festivalA);
            em.persist(festivalEarlier);
            em.flush();
            em.clear();

            // when
            Page<Festival> result = festivalRepository.findActiveFestivals(
                    PageRequest.of(0, 10), LocalDate.now());

            // then
            List<Festival> festivals = result.getContent();
            assertThat(festivals).hasSize(3);
            assertThat(festivals.get(0).getTitle()).isEqualTo("이른 시작 페스티벌"); // startDate가 가장 빠름
            assertThat(festivals.get(1).getTitle()).isEqualTo("A페스티벌");           // 같은 startDate, title ASC
            assertThat(festivals.get(2).getTitle()).isEqualTo("C페스티벌");
        }
    }

    private Festival createFestival(String title, LocalDate startDate, LocalDate endDate) {
        return Festival.builder()
                .title(title)
                .mainImageUrl("https://example.com/img.jpg")
                .location("서울")
                .startDate(startDate)
                .endDate(endDate)
                .startTime(LocalTime.of(18, 0))
                .status(FestivalStatus.UPCOMING)
                .organizer(organizer)
                .build();
    }

    private Audience createAudience(String email) {
        return Audience.builder()
                .email(email)
                .profileImageUrl("https://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_" + email)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();
    }

    private Organizer createOrganizer(String email) {
        return Organizer.builder()
                .email(email)
                .profileImageUrl("https://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_" + email)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();
    }
}
