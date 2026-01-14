package com.amp.domain.stage.service;

import com.amp.domain.festival.entity.Festival;
import com.amp.domain.stage.dto.request.StageRequest;
import com.amp.domain.stage.entity.Stage;
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
@RequiredArgsConstructor
@Transactional
public class StageService {

    public void syncStages(Festival festival, List<StageRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<Stage> existStages = festival.getStages();
        Map<Long, Stage> stageMap = existStages.stream()
                .collect(Collectors.toMap(Stage::getId, Function.identity()));

        Set<Long> requestIds = requests.stream()
                .map(StageRequest::getId).filter(Objects::nonNull).collect(Collectors.toSet());

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
}
