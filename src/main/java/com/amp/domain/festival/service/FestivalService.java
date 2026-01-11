package com.amp.domain.festival.service;

import com.amp.domain.festival.dto.request.FestivalCreateRequest;
import com.amp.domain.festival.dto.response.FestivalCreateResponse;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.stage.repository.StageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final StageRepository stageRepository;

    @Transactional
    public FestivalCreateResponse createFestival(FestivalCreateRequest request) {
        return null;
    }

}
