package com.amp.domain.festival.service;

import com.amp.domain.festival.dto.response.OrganizerActiveFestivalPageResponse;
import com.amp.domain.festival.dto.response.OrganizerFestivalPageResponse;
import com.amp.domain.festival.dto.response.OrganizerFestivalSummaryResponse;
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

import java.util.List;


@RequiredArgsConstructor
@Service
public class OrganizerFestivalService {

    private final FestivalRepository festivalRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public OrganizerFestivalPageResponse getMyFestivals(Pageable pageable) {
        User user = authService.getCurrentUser();

        Page<Festival> festivalPage = festivalRepository.findAllByMyUser(user, pageable);
        Page<OrganizerFestivalSummaryResponse> summaryPage = festivalPage.map(OrganizerFestivalSummaryResponse::from);
        return OrganizerFestivalPageResponse.of(summaryPage);
    }

    @Transactional(readOnly = true)
    public OrganizerActiveFestivalPageResponse getActiveFestivals(Pageable pageable) {
        User user = authService.getCurrentUser();

        long ongoingCount = festivalRepository.countByOrganizerAndStatus(user, FestivalStatus.ONGOING);
        long upcomingCount = festivalRepository.countByOrganizerAndStatus(user, FestivalStatus.UPCOMING);

        Page<Festival> activePage = festivalRepository.findActiveFestivalsByUser(user, List.of(FestivalStatus.ONGOING, FestivalStatus.UPCOMING), pageable);
        return OrganizerActiveFestivalPageResponse.of(ongoingCount, upcomingCount, activePage);
    }
}
