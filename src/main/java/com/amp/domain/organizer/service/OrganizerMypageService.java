package com.amp.domain.organizer.service;

import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.festival.repository.FestivalRepository;
import com.amp.domain.organizer.dto.response.OrganizerMypageResponse;
import com.amp.domain.user.entity.Organizer;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.OrganizerRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerMypageService {

    private final OrganizerRepository organizerRepository;
    private final FestivalRepository festivalRepository;

    public OrganizerMypageResponse getOrganizerMypage(Long userId) {
        Organizer organizer = organizerRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Long ongoingCount = festivalRepository.countFestivalsByUserIdAndStatus(
                userId,
                FestivalStatus.ONGOING
        );

        Long upcomingCount = festivalRepository.countFestivalsByUserIdAndStatus(
                userId,
                FestivalStatus.UPCOMING
        );

        return OrganizerMypageResponse.builder()
                .organizerName(organizer.getOrganizerName())
                .profileImageUrl(organizer.getProfileImageUrl())
                .ongoingFestivalCount(ongoingCount)
                .upcomingFestivalCount(upcomingCount)
                .build();
    }
}
