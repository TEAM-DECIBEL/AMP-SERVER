package com.amp.domain.stage.repository;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.stage.entity.CongestionLevel;
import com.amp.domain.stage.entity.Stage;
import com.amp.domain.stage.entity.UserCongestionReport;
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
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
@DisplayName("UserCongestionReportRepository 테스트")
class UserCongestionReportRepositoryTest {

    @Autowired
    private UserCongestionReportRepository repository;

    @Autowired
    private TestEntityManager em;

    private Stage stage;
    private User reportingUser;

    @BeforeEach
    void setUp() {
        User organizer = createUser("organizer@test.com");
        em.persist(organizer);

        Festival festival = Festival.builder()
                .title("테스트 페스티벌")
                .mainImageUrl("https://example.com/img.jpg")
                .location("서울")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(18, 0))
                .status(FestivalStatus.ONGOING)
                .organizer(organizer)
                .build();
        em.persist(festival);

        stage = Stage.builder()
                .festival(festival)
                .title("메인 스테이지")
                .location("A구역")
                .build();
        em.persist(stage);

        reportingUser = createUser("user@test.com");
        em.persist(reportingUser);

        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("findRecentReports - 기본 필터링")
    class BasicFilter {

        @Test
        @DisplayName("1시간 이내 제보는 조회된다")
        void withinOneHourSuccess() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reportedAt = now.minusMinutes(30);
            LocalDateTime oneHourAgo = now.minusHours(1);

            em.persist(buildReport(reportingUser, stage, reportedAt, CongestionLevel.CROWDED));
            em.flush();
            em.clear();

            // when
            List<UserCongestionReport> results = repository.findRecentReports(stage.getId(), oneHourAgo);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getReportedLevel()).isEqualTo(CongestionLevel.CROWDED);
        }

        @Test
        @DisplayName("정확히 1시간 전 제보는 포함된다 (>= 경계값)")
        void exactlyOneHourAgoIncluded() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);

            // reportedAt = oneHourAgo (경계값, >= 이므로 포함)
            em.persist(buildReport(reportingUser, stage, oneHourAgo, CongestionLevel.NORMAL));
            em.flush();
            em.clear();

            // when
            List<UserCongestionReport> results = repository.findRecentReports(stage.getId(), oneHourAgo);

            // then
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("1시간 초과 제보는 조회되지 않는다")
        void exceedsOneHourExcluded() {
            // given
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reportedAt = now.minusMinutes(90); // 90분 전
            LocalDateTime oneHourAgo = now.minusHours(1);

            em.persist(buildReport(reportingUser, stage, reportedAt, CongestionLevel.SMOOTH));
            em.flush();
            em.clear();

            // when
            List<UserCongestionReport> results = repository.findRecentReports(stage.getId(), oneHourAgo);

            // then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("다른 stage의 제보는 조회되지 않는다")
        void otherStageExcluded() {
            // given
            User organizer2 = createUser("organizer2@test.com");
            em.persist(organizer2);

            Festival festival2 = Festival.builder()
                    .title("다른 페스티벌")
                    .mainImageUrl("https://example.com/img2.jpg")
                    .location("부산")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(14, 0))
                    .status(FestivalStatus.ONGOING)
                    .organizer(organizer2)
                    .build();
            em.persist(festival2);

            Stage otherStage = Stage.builder()
                    .festival(festival2)
                    .title("다른 스테이지")
                    .location("B구역")
                    .build();
            em.persist(otherStage);

            LocalDateTime now = LocalDateTime.now();
            em.persist(buildReport(reportingUser, otherStage, now.minusMinutes(10), CongestionLevel.CROWDED));
            em.flush();
            em.clear();

            // when
            List<UserCongestionReport> results = repository.findRecentReports(stage.getId(), now.minusHours(1));

            // then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("findRecentReports - CURRENT_DATE 조건 제거 검증")
    class CurrentDateFix {

        @Test
        @DisplayName("자정을 넘긴 경우에도 1시간 이내 제보는 조회된다")
        void crossingMidnightWithinOneHourSuccess() {
            // given
            // 시뮬레이션: 현재 시각이 00:30이고 1시간 전은 어제 23:30
            // reportedAt = 어제 23:45 (날짜는 어제이지만 1시간 이내)
            LocalDateTime yesterday2330 = LocalDate.now().minusDays(1).atTime(23, 30, 0);
            LocalDateTime yesterday2345 = LocalDate.now().minusDays(1).atTime(23, 45, 0);

            em.persist(buildReport(reportingUser, stage, yesterday2345, CongestionLevel.SMOOTH));
            em.flush();
            em.clear();

            // when
            // oneHourAgo = 어제 23:30 → reportedAt(23:45) >= oneHourAgo(23:30) 이므로 포함
            List<UserCongestionReport> results = repository.findRecentReports(stage.getId(), yesterday2330);

            // then
            // CURRENT_DATE 조건이 제거된 덕분에 날짜가 달라도 시간 조건만 만족하면 조회됨
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("CURRENT_DATE 조건이 제거되어 날짜와 무관하게 시간 범위만으로 필터링된다")
        void onlyTimeRangeFilterNoDayBoundary() {
            // given - oneHourAgo를 3일 전으로 설정 (실제 날짜 경계와 무관하게 필터링 검증)
            LocalDateTime cutoff = LocalDateTime.now().minusDays(3);           // 기준점: 3일 전
            LocalDateTime included = cutoff.plusMinutes(30);  // cutoff 이후 → 포함
            LocalDateTime excluded = cutoff.minusMinutes(30); // cutoff 이전 → 제외

            em.persist(buildReport(reportingUser, stage, included, CongestionLevel.CROWDED));
            em.persist(buildReport(reportingUser, stage, excluded, CongestionLevel.NORMAL));
            em.flush();
            em.clear();

            // when - 3일 전을 기준으로 조회 (CURRENT_DATE 조건이 없으므로 오늘 날짜와 무관)
            List<UserCongestionReport> results = repository.findRecentReports(stage.getId(), cutoff);

            // then - cutoff 이후 제보(3일 전 +30분)만 포함, cutoff 이전(3일 전 -30분)은 제외
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getReportedLevel()).isEqualTo(CongestionLevel.CROWDED);
        }
    }

    @Nested
    @DisplayName("findRecentReports - 정렬 검증")
    class Ordering {

        @Test
        @DisplayName("최신 제보 순(reportedAt DESC)으로 정렬된다")
        void orderedByReportedAtDesc() {
            // given
            LocalDateTime now = LocalDateTime.now();
            UserCongestionReport older = buildReport(reportingUser, stage, now.minusMinutes(40), CongestionLevel.CROWDED);
            UserCongestionReport newer = buildReport(reportingUser, stage, now.minusMinutes(10), CongestionLevel.SMOOTH);

            em.persist(older);
            em.persist(newer);
            em.flush();
            em.clear();

            // when
            List<UserCongestionReport> results = repository.findRecentReports(stage.getId(), now.minusHours(1));

            // then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getReportedLevel()).isEqualTo(CongestionLevel.SMOOTH);  // 더 최근
            assertThat(results.get(1).getReportedLevel()).isEqualTo(CongestionLevel.CROWDED); // 더 과거
        }
    }

    private UserCongestionReport buildReport(User user, Stage stage, LocalDateTime reportedAt, CongestionLevel level) {
        return UserCongestionReport.builder()
                .user(user)
                .stage(stage)
                .reportedLevel(level)
                .reportedAt(reportedAt)
                .build();
    }

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .nickname("테스트유저_" + email.split("@")[0])
                .profileImageUrl("https://example.com/profile.jpg")
                .provider(AuthProvider.GOOGLE)
                .providerId("google_" + email)
                .isActive(true)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .userType(UserType.AUDIENCE)
                .build();
    }
}
