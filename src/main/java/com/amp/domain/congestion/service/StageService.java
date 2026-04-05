package com.amp.domain.congestion.service;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.congestion.dto.request.StageRequest;
import com.amp.domain.congestion.entity.Stage;
import com.amp.domain.congestion.repository.AudienceCongestionReportRepository;
import com.amp.domain.congestion.repository.StageCongestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StageService {

    private final StageCongestionRepository stageCongestionRepository;
    private final AudienceCongestionReportRepository audienceReportRepository;

    public void syncStages(Festival festival, List<StageRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<Stage> existStages = festival.getStages();
        Map<Long, Stage> stageMap = existStages.stream()
                .collect(Collectors.toMap(Stage::getId, Function.identity()));

        Set<Long> requestIds = requests.stream()
                .map(StageRequest::getId).filter(Objects::nonNull).collect(Collectors.toSet());

        List<Long> removedStageIds = existStages.stream()
                .map(Stage::getId)
                .filter(id -> !requestIds.contains(id))
                .collect(Collectors.toList());

        if (!removedStageIds.isEmpty()) {
            stageCongestionRepository.deleteByStageIdIn(removedStageIds);
        }

        existStages.removeIf(stage -> !requestIds.contains(stage.getId()));

        for (StageRequest request : requests) {
            if (request.getId() != null && stageMap.containsKey(request.getId())) {
                stageMap.get(request.getId()).update(request.getTitle(), request.getLocation());
            } else {
                existStages.add(Stage.builder()
                        .festival(festival)
                        .title(request.getTitle())
                        .location(request.getLocation())
                        .build());
            }
        }
    }

    public void clearCongestionData(List<Long> stageIds) {
        if (stageIds == null || stageIds.isEmpty()) {
            return;
        }
        audienceReportRepository.deleteByStageIdIn(stageIds);
        stageCongestionRepository.deleteByStageIdIn(stageIds);
    }
}
