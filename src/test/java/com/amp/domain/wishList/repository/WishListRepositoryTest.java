package com.amp.domain.wishList.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.entity.UserFestival;
import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
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

    private User audience;
    private User organizer;

    @BeforeEach
    void setUp() {
        organizer = createUser("organizer@test.com", UserType.ORGANIZER);
        em.persist(organizer);

        audience = createUser("audience@test.com", UserType.AUDIENCE);
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

            UserFestival userFestival = UserFestival.builder()
                    .user(audience)
                    .festival(festival)
                    .wishList(true)
                    .build();
            em.persist(userFestival);
            em.flush();
            em.clear();

            // when
            Page<UserFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
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

            UserFestival userFestival = UserFestival.builder()
                    .user(audience)
                    .festival(festival)
                    .wishList(false) // wishList = false
                    .build();
            em.persist(userFestival);
            em.flush();
            em.clear();

            // when
            Page<UserFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("endDate가 today보다 이전인 종료된 페스티벌은 조회되지 않는다")
        void endedFestival_Excluded() {
            // given
            Festival endedFestival = createFestival("종료된 위시 페스티벌",
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1)); // 어제 종료
            em.persist(endedFestival);

            UserFestival userFestival = UserFestival.builder()
                    .user(audience)
                    .festival(endedFestival)
                    .wishList(true)
                    .build();
            em.persist(userFestival);
            em.flush();
            em.clear();

            // when
            Page<UserFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("soft delete된 페스티벌의 UserFestival은 조회되지 않는다")
        void softDeletedFestival_Excluded() {
            // given
            Festival festival = createFestival("삭제 예정 페스티벌",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(festival);

            UserFestival userFestival = UserFestival.builder()
                    .user(audience)
                    .festival(festival)
                    .wishList(true)
                    .build();
            em.persist(userFestival);
            em.flush();

            festival.delete(); // soft delete
            em.flush();
            em.clear();

            // when
            Page<UserFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then - f.deletedAt IS NULL 조건으로 필터링됨
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("다른 사용자의 위시리스트는 조회되지 않는다")
        void otherUserWishList_Excluded() {
            // given
            User otherUser = createUser("other@test.com", UserType.AUDIENCE);
            em.persist(otherUser);

            Festival festival = createFestival("다른 유저 위시 페스티벌",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(festival);

            UserFestival otherUserFestival = UserFestival.builder()
                    .user(otherUser)
                    .festival(festival)
                    .wishList(true)
                    .build();
            em.persist(otherUserFestival);
            em.flush();
            em.clear();

            // when - audience(다른 유저)로 조회
            Page<UserFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("today 파라미터로 과거 날짜를 전달하면 종료된 페스티벌도 조회된다")
        void pastDateParam_IncludesEndedFestivals() {
            // given
            Festival endedFestival = createFestival("지난 페스티벌",
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
            em.persist(endedFestival);

            UserFestival userFestival = UserFestival.builder()
                    .user(audience)
                    .festival(endedFestival)
                    .wishList(true)
                    .build();
            em.persist(userFestival);
            em.flush();
            em.clear();

            // when - today를 1주일 전으로 전달
            Page<UserFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now().minusWeeks(1));

            // then - 파라미터로 날짜를 직접 제어 가능함을 검증
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("여러 조건을 동시에 만족하는 경우만 조회된다")
        void multipleConditions_OnlyMatchingReturned() {
            // given
            // 조회 대상: wishList=true, 종료 안됨
            Festival activeFestival = createFestival("활성 위시 페스티벌",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(activeFestival);
            em.persist(UserFestival.builder().user(audience).festival(activeFestival).wishList(true).build());

            // 제외 대상 1: wishList=false
            Festival festival2 = createFestival("위시 미등록",
                    LocalDate.now().plusDays(1), LocalDate.now().plusDays(3));
            em.persist(festival2);
            em.persist(UserFestival.builder().user(audience).festival(festival2).wishList(false).build());

            // 제외 대상 2: 종료됨
            Festival endedFestival = createFestival("종료된 페스티벌",
                    LocalDate.now().minusDays(5), LocalDate.now().minusDays(1));
            em.persist(endedFestival);
            em.persist(UserFestival.builder().user(audience).festival(endedFestival).wishList(true).build());

            em.flush();
            em.clear();

            // when
            Page<UserFestival> result = wishListRepository.findAllByUserIdAndWishListTrue(
                    audience.getId(), PageRequest.of(0, 10), LocalDate.now());

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFestival().getTitle()).isEqualTo("활성 위시 페스티벌");
        }
    }

    // ===== 헬퍼 메서드 =====

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

    private User createUser(String email, UserType userType) {
        return User.builder()
                .email(email)
                .nickname("유저_" + email.split("@")[0])
                .profileImageUrl("https://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_" + email)
                .isActive(true)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(userType)
                .build();
    }
}
