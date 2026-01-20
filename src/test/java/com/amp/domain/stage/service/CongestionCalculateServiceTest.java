package com.amp.domain.stage.service;

import com.amp.domain.stage.entity.CongestionLevel;
import com.amp.domain.stage.entity.Stage;
import com.amp.domain.stage.entity.UserCongestionReport;
import com.amp.domain.stage.repository.StageCongestionRepository;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.stage.repository.UserCongestionReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CongestionCalculateServiceTest {

    @InjectMocks
    private CongestionCalculateService calculateService;

    @Mock
    private UserCongestionReportRepository reportRepository;
    @Mock
    private StageCongestionRepository stageCongestionRepository;
    @Mock
    private StageRepository stageRepository;

    @Test
    @DisplayName("최신 제보에 더 높은 가중치를 주어 혼잡도를 계산한다")
    void calculateWeightedAverageTest() {
        // given
        Long stageId = 1L;
        Stage stage = Stage.builder().id(stageId).build();
        LocalDateTime now = LocalDateTime.now();

        // 10분 전 제보 (가중치 1.0, 혼잡 3점)
        UserCongestionReport report1 = UserCongestionReport.builder()
                .reportedLevel(CongestionLevel.CROWDED) // 3점
                .reportedAt(now.minusMinutes(10))
                .build();

        // 50분 전 제보 (가중치 0.25, 여유 1점)
        UserCongestionReport report2 = UserCongestionReport.builder()
                .reportedLevel(CongestionLevel.SMOOTH) // 1점
                .reportedAt(now.minusMinutes(50))
                .build();

        given(stageRepository.findById(stageId)).willReturn(Optional.of(stage));
        given(reportRepository.findRecentReports(eq(stageId), any())).willReturn(List.of(report1, report2));

        // when
        calculateService.calculateAndSave(stageId);

        // then
        // 수식: (1.0 * 3 + 0.25 * 1) / (1.0 + 0.25) = 3.25 / 1.25 = 2.6
        // 2.6점은 CongestionLevel.fromScore에 의해 CROWDED로 저장되어야 함
        verify(stageCongestionRepository).save(argThat(sc ->
                sc.getCongestionLevel() == CongestionLevel.CROWDED
        ));
    }

}