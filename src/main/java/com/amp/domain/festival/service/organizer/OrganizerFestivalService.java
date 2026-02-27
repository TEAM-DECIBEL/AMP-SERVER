package com.amp.domain.festival.service.organizer;

import com.amp.domain.festival.dto.response.OrganizerActiveFestivalPageResponse;
import com.amp.domain.festival.dto.response.OrganizerFestivalListResponse;
import com.amp.domain.festival.entity.Festival;
import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.user.entity.Organizer;
import com.amp.global.common.dto.response.PageResponse;
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
    public PageResponse<OrganizerFestivalListResponse> getMyFestivals(Pageable pageable) {
        Organizer organizer = (Organizer) authService.getCurrentUser();

        Page<Festival> festivalPage = festivalRepository.findAllByMyUser(organizer, pageable);
        Page<OrganizerFestivalListResponse> summaryPage = festivalPage.map(OrganizerFestivalListResponse::from);
        return PageResponse.of(summaryPage);
    }

    @Transactional(readOnly = true)
    public OrganizerActiveFestivalPageResponse getActiveFestivals(Pageable pageable) {
        Organizer organizer = (Organizer) authService.getCurrentUser();

        long ongoingCount = festivalRepository.countByOrganizerAndStatus(organizer, FestivalStatus.ONGOING);
        long upcomingCount = festivalRepository.countByOrganizerAndStatus(organizer, FestivalStatus.UPCOMING);

        Page<Festival> activePage = festivalRepository.findActiveFestivalsByUser(organizer, List.of(FestivalStatus.ONGOING, FestivalStatus.UPCOMING), pageable);
        return OrganizerActiveFestivalPageResponse.of(ongoingCount, upcomingCount, activePage);
    }
}
