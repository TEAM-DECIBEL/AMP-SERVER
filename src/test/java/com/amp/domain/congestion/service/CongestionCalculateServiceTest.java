package com.amp.domain.congestion.service;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalSchedule;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.congestion.entity.CongestionLevel;
import com.amp.domain.congestion.entity.Stage;
import com.amp.domain.congestion.entity.AudienceCongestionReport;
import com.amp.domain.congestion.repository.StageCongestionRepository;
import com.amp.domain.congestion.repository.StageRepository;
import com.amp.domain.congestion.repository.AudienceCongestionReportRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private AudienceCongestionReportRepository reportRepository;
    @Mock
    private StageCongestionRepository stageCongestionRepository;
    @Mock
    private StageRepository stageRepository;
    @Mock
    private FestivalScheduleRepository festivalScheduleRepository;
    @Mock
    private Festival festival;

    @Test
    @DisplayName("최신 제보에 더 높은 가중치를 주어 혼잡도를 계산한다")
    void calculateWeightedAverageTest() {
        // given
        Long stageId = 1L;
        Long festivalId = 1L;
        Stage stage = Stage.builder().id(stageId).festival(festival).build();
        LocalDateTime now = LocalDateTime.now();

        // 10분 전 제보 (가중치 1.0, 혼잡 3점)
        AudienceCongestionReport report1 = AudienceCongestionReport.builder()
                .reportedLevel(CongestionLevel.CROWDED) // 3점
                .reportedAt(now.minusMinutes(10))
                .build();

        // 50분 전 제보 (가중치 0.25, 여유 1점)
        AudienceCongestionReport report2 = AudienceCongestionReport.builder()
                .reportedLevel(CongestionLevel.SMOOTH) // 1점
                .reportedAt(now.minusMinutes(50))
                .build();

        given(festival.getId()).willReturn(festivalId);
        given(festivalScheduleRepository.findByFestivalIdAndFestivalDate(eq(festivalId), any()))
                .willReturn(Optional.of(FestivalSchedule.builder()
                        .festival(festival)
                        .festivalDate(LocalDate.now())
                        .festivalTime(LocalTime.of(0, 0))
                        .build()));
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