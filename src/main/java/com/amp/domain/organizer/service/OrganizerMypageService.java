package com.amp.domain.organizer.service;

import com.amp.domain.festival.entity.FestivalStatus;
import com.amp.domain.organizer.dto.response.OrganizerMypageResponse;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.organizer.exception.OrganizerErrorCode;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerMypageService {

    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;


    public OrganizerMypageResponse getOrganizerMypage(Long userId) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 주최사 정보 조회
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(OrganizerErrorCode.ORGANIZER_NOT_FOUND));

        // 진행 중인 공연 수 조회
        Integer ongoingCount = organizerRepository.countFestivalsByOrganizerIdAndStatus(
                organizer.getId(),
                FestivalStatus.ONGOING
        );

        // 진행 예정 공연 수 조회
        Integer upcomingCount = organizerRepository.countFestivalsByOrganizerIdAndStatus(
                organizer.getId(),
                FestivalStatus.UPCOMING
        );

        return OrganizerMypageResponse.builder()
                .organizerName(organizer.getOrganizerName())
                .profileImageUrl(user.getProfileImageUrl())
                .ongoingFestivalCount(ongoingCount)
                .upcomingFestivalCount(upcomingCount)
                .build();
    }

}