package com.amp.domain.stage.service;

import com.amp.domain.festival.exception.FestivalErrorCode;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.festival.repository.FestivalScheduleRepository;
import com.amp.domain.stage.dto.response.FestivalCongestionResponse;
import com.amp.domain.stage.dto.response.StageCongestionSummary;
import com.amp.domain.stage.entity.CongestionLevel;
import com.amp.domain.stage.entity.Stage;
import com.amp.domain.stage.entity.StageCongestion;
import com.amp.domain.stage.repository.StageCongestionRepository;
import com.amp.domain.stage.repository.StageRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.global.exception.CustomException;
import com.amp.global.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CongestionQueryService {

    private final AuthService authService;
    private final StageRepository stageRepository;
    private final FestivalScheduleRepository festivalScheduleRepository;
    private final StageCongestionRepository stageCongestionRepository;
    private final FestivalRepository festivalRepository;

    public FestivalCongestionResponse getFestivalCongestion(Long festivalId, Pageable pageable) {
        if (!festivalRepository.existsById(festivalId)) {
            throw new CustomException(FestivalErrorCode.FESTIVAL_NOT_FOUND);
        }

        boolean isInputAvailable = checkInputStatus(festivalId);
        Page<Stage> stagePage = stageRepository.findByFestivalId(festivalId, pageable);

        List<Long> stageIds = stagePage.getContent().stream()
                .map(Stage::getId)
                .toList();

        Map<Long, StageCongestion> latestCongestionMap = stageCongestionRepository.findLatestByStageIds(stageIds)
                .stream()
                .collect(Collectors.toMap(sc -> sc.getStage().getId(), sc -> sc));

        List<StageCongestionSummary> summaries = stagePage.getContent().stream()
                .map(stage -> mapToSummary(stage, latestCongestionMap.get(stage.getId()), isInputAvailable))
                .toList();

        return FestivalCongestionResponse.of(isInputAvailable, summaries, stagePage);
    }

    private StageCongestionSummary mapToSummary(Stage stage, StageCongestion congestion, boolean isInputAvailable) {
        boolean isNone = !isInputAvailable || congestion == null || congestion.getCongestionLevel() == CongestionLevel.NONE;

        return StageCongestionSummary.builder()
                .stageId(stage.getId())
                .title(stage.getTitle())
                .location(stage.getLocation())
                .congestionLevel(isNone ? CongestionLevel.NONE.name() : congestion.getCongestionLevel().name())
                .build();
    }

    private boolean checkInputStatus(Long festivalId) {
        User user = authService.getCurrentUserOrNull();
        if (user == null || user.getUserType() == UserType.ORGANIZER) return false;

        return festivalScheduleRepository.findByFestivalIdAndFestivalDate(festivalId, LocalDate.now())
                .map(s -> LocalDateTime.now().isAfter(LocalDate.now().atTime(s.getFestivalTime()).minusHours(8)))
                .orElse(false);
    }

}
