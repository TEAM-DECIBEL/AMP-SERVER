package com.amp.domain.wishList.repository;

import com.amp.domain.festival.entity.AudienceFestival;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.user.entity.Audience;
import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.Organizer;
import com.amp.domain.user.entity.RegistrationStatus;
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
@DisplayName("WishListRepository 테스트")
class WishListRepositoryTest {

    @Autowired
    private WishListRepository wishListRepository;

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
    @DisplayName("findAllByUserIdAndWishListTrue - :today 파라미터 검증")
    class FindAllByUserIdAndWishListTrue {

        @Test
        @DisplayName("wishList=true이고 endDate >= today인 UserFestival은 조회된다")
        void wishListTrueAndActiveFestival_Included() {
            // given
            Festival festival = createFestival("진행 중 위시 페스티벌",
                    LocalDate.now().minusDays(1), LocalDate.now().plusDays(2));
            em.persist(festival);

            AudienceFestival userFestival = AudienceFestival.builder()
                    .audience(audience)
                    .festival(festival)
                    .wishList(true)
                    .build();
            em.persist(userFestival);
            em.flush();
            em.clear();

            // when
            Page<AudienceFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFestival().getTitle()).isEqualTo("진행 중 위시 페스티벌");
        }

        @Test
        @DisplayName("wishList=false인 UserFestival은 조회되지 않는다")
        void wishListFalse_Excluded() {
            // given
            Festival festival = createFestival("위시 미등록 페스티벌",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(festival);

            AudienceFestival userFestival = AudienceFestival.builder()
                    .audience(audience)
                    .festival(festival)
                    .wishList(false)
                    .build();
            em.persist(userFestival);
            em.flush();
            em.clear();

            // when
            Page<AudienceFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("endDate가 today보다 이전인 종료된 페스티벌은 조회되지 않는다")
        void endedFestivalExcluded() {
            // given
            Festival endedFestival = createFestival("종료된 위시 페스티벌",
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
            em.persist(endedFestival);

            AudienceFestival userFestival = AudienceFestival.builder()
                    .audience(audience)
                    .festival(endedFestival)
                    .wishList(true)
                    .build();
            em.persist(userFestival);
            em.flush();
            em.clear();

            // when
            Page<AudienceFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("soft delete된 페스티벌의 UserFestival은 조회되지 않는다")
        void softDeletedFestivalExcluded() {
            // given
            Festival festival = createFestival("삭제 예정 페스티벌",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(festival);

            AudienceFestival userFestival = AudienceFestival.builder()
                    .audience(audience)
                    .festival(festival)
                    .wishList(true)
                    .build();
            em.persist(userFestival);
            em.flush();

            festival.delete();
            em.flush();
            em.clear();

            // when
            Page<AudienceFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("다른 사용자의 위시리스트는 조회되지 않는다")
        void otherUserWishListExcluded() {
            // given
            Audience otherUser = createAudience("other@test.com");
            em.persist(otherUser);

            Festival festival = createFestival("다른 유저 위시 페스티벌",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(festival);

            AudienceFestival otherUserFestival = AudienceFestival.builder()
                    .audience(otherUser)
                    .festival(festival)
                    .wishList(true)
                    .build();
            em.persist(otherUserFestival);
            em.flush();
            em.clear();

            // when
            Page<AudienceFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("today 파라미터로 과거 날짜를 전달하면 종료된 페스티벌도 조회된다")
        void pastDateParamIncludesEndedFestivals() {
            // given
            Festival endedFestival = createFestival("지난 페스티벌",
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
            em.persist(endedFestival);

            AudienceFestival userFestival = AudienceFestival.builder()
                    .audience(audience)
                    .festival(endedFestival)
                    .wishList(true)
                    .build();
            em.persist(userFestival);
            em.flush();
            em.clear();

            // when
            Page<AudienceFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now().minusWeeks(1));

            // then
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("여러 조건을 동시에 만족하는 경우만 조회된다")
        void multipleConditionsOnlyMatchingReturned() {
            // given
            Festival activeFestival = createFestival("활성 위시 페스티벌",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(activeFestival);
            em.persist(AudienceFestival.builder().audience(audience).festival(activeFestival).wishList(true).build());

            // 제외 대상 1: wishList=false
            Festival festival2 = createFestival("위시 미등록",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(festival2);
            em.persist(AudienceFestival.builder().audience(audience).festival(festival2).wishList(false).build());

            // 제외 대상 2: 종료됨
            Festival endedFestival = createFestival("종료된 페스티벌",
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
            em.persist(endedFestival);
            em.persist(AudienceFestival.builder().audience(audience).festival(endedFestival).wishList(true).build());

            em.flush();
            em.clear();

            // when
            Page<AudienceFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFestival().getTitle()).isEqualTo("활성 위시 페스티벌");
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
