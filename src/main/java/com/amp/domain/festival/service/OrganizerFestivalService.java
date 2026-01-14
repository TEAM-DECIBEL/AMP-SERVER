package com.amp.domain.festival.service;

import com.amp.domain.festival.dto.response.ActiveFestivalPageResponse;
import com.amp.domain.festival.dto.response.FestivalPageResponse;
import com.amp.domain.festival.dto.response.FestivalSummaryResponse;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.user.entity.User;
import com.amp.global.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class OrganizerFestivalService {

    private final FestivalRepository festivalRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public FestivalPageResponse getMyFestivals(Pageable pageable) {
        User user = authService.getCurrentUser();

        Page<Festival> festivalPage = festivalRepository.findAllByMyUser(user, pageable);
        Page<FestivalSummaryResponse> summaryPage = festivalPage.map(FestivalSummaryResponse::from);
        return FestivalPageResponse.of(summaryPage);
    }

    @Transactional(readOnly = true)
    public ActiveFestivalPageResponse getActiveFestivals(Pageable pageable) {
        User user = authService.getCurrentUser();

        long ongoingCount = festivalRepository.countByOrganizerAndStatus(user, FestivalStatus.ONGOING);
        long upcomingCount = festivalRepository.countByOrganizerAndStatus(user, FestivalStatus.UPCOMING);

        Page<Festival> activePage = festivalRepository.findActiveFestivalsByUser(user, pageable);
        return ActiveFestivalPageResponse.of(ongoingCount, upcomingCount, activePage);
    }
}
