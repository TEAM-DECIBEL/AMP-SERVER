package com.amp.domain.stage.service;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.stage.entity.CongestionLevel;
import com.amp.domain.stage.entity.Stage;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import com.amp.global.security.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
class CongestionReportIntegrationTest {

    @MockitoBean // 👈 실제 빈 대신 가짜 빈을 주입
    private AuthService authService;

    @Autowired private CongestionReportService reportService;
    @Autowired private UserRepository userRepository;
    @Autowired private StageRepository stageRepository;
    @Autowired private FestivalRepository festivalRepository;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    private Long savedStageId;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        // 1. DB에 유저 저장
        User user = userRepository.save(User.builder().email("test@test.com").userType(UserType.AUDIENCE).build());

        // 2. AuthService가 항상 이 유저를 반환하도록 설정
        given(authService.getCurrentUser()).willReturn(user);

        // 3. 스테이지 저장
        Festival festival = festivalRepository.save(Festival.builder().title("축제").build());
        Stage stage = stageRepository.save(Stage.builder().festival(festival).title("무대").build());
        savedStageId = stage.getId();
    }

    @Test
    @DisplayName("동일 유저가 15분 이내에 중복 보고 시 예외가 발생한다")
    void duplicateReportTest() {
        reportService.reportCongestion(savedStageId, CongestionLevel.NORMAL);

        assertThatThrownBy(() -> reportService.reportCongestion(savedStageId, CongestionLevel.CROWDED))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("ALREADY_REPORTED_RECENTLY");
    }
}