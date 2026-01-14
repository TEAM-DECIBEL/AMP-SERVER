package com.amp.domain.auth.service;

import com.amp.domain.auth.dto.OnboardingRequest;
import com.amp.domain.auth.dto.OnboardingResponse;
import com.amp.domain.auth.dto.OnboardingStatusResponse;
import com.amp.domain.organizer.entity.Organizer;
import com.amp.domain.organizer.repository.OrganizerRepository;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.amp.global.common.CommonErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingService {

    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;


    public OnboardingResponse completeOnboarding(String email, OnboardingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 이미 온보딩 완료된 경우
        if (user.getRegistrationStatus() == RegistrationStatus.COMPLETED) {
            throw new CustomException(ALREADY_COMPLETED_ONBOARDING);
        }

        // OAuth2 핸들러에서 설정한 UserType과 요청의 UserType이 일치하는지 확인
        if (user.getUserType() != request.getUserType()) {
            log.warn("UserType mismatch - stored: {}, requested: {}",
                    user.getUserType(), request.getUserType());
            throw new CustomException(INVALID_USER_TYPE);
        }

        // 사용자 타입에 따라 온보딩 처리
        if (request.getUserType() == UserType.AUDIENCE) {
            completeAudienceOnboarding(user, request);
        } else if (request.getUserType() == UserType.ORGANIZER) {
            completeOrganizerOnboarding(user, request);
        } else {
            throw new CustomException(INVALID_USER_TYPE);
        }

        return OnboardingResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userType(user.getUserType())
                .registrationStatus(user.getRegistrationStatus())
                .message("온보딩이 완료되었습니다.")
                .build();
    }


    private void completeAudienceOnboarding(User user, OnboardingRequest request) {
        log.info("Completing audience onboarding for user: {}", user.getEmail());

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(DUPLICATE_NICKNAME);
        }

        user.completeAudienceOnboarding(request.getNickname());
        userRepository.save(user);
    }


    private void completeOrganizerOnboarding(User user, OnboardingRequest request) {
        log.info("Completing organizer onboarding for user: {}", user.getEmail());

        // 주최사명 필수 체크
        if (request.getOrganizerName() == null || request.getOrganizerName().isBlank()) {
            throw new CustomException(ORGANIZER_NAME_REQUIRED);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(DUPLICATE_NICKNAME);
        }

        // User 업데이트
        user.completeOrganizerOnboarding(request.getNickname());
        userRepository.save(user);

        // Organizer 엔티티 생성
        // Note: Festival은 나중에 연결하거나, 초기값 null로 설정
        Organizer organizer = Organizer.builder()
                .user(user)
                .festival(null) // 추후 페스티벌 등록 시 연결
                .organizerName(request.getOrganizerName())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .description(request.getDescription())
                .build();

        organizerRepository.save(organizer);

        log.info("Created organizer entity for user: {}, organizer name: {}",
                user.getEmail(), request.getOrganizerName());
    }


    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        return OnboardingStatusResponse.builder()
                .email(user.getEmail())
                .registrationStatus(user.getRegistrationStatus())
                .userType(user.getUserType())
                .needsOnboarding(user.getRegistrationStatus() == RegistrationStatus.PENDING)
                .build();
    }
}