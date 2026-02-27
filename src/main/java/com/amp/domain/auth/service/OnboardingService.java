package com.amp.domain.auth.service;

import com.amp.domain.auth.dto.OnboardingRequest;
import com.amp.domain.auth.dto.OnboardingResponse;
import com.amp.domain.auth.dto.OnboardingStatusResponse;
import com.amp.domain.auth.exception.OnboardingErrorCode;
import com.amp.domain.auth.exception.OnboardingException;
import com.amp.domain.user.entity.Organizer;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.OrganizerRepository;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.common.CommonErrorCode;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OnboardingService {

    private final UserRepository userRepository;
    private final OrganizerRepository organizerRepository;

    public OnboardingResponse completeOnboarding(String email, OnboardingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));

        // 이미 온보딩 완료된 경우
        if (user.getRegistrationStatus() == RegistrationStatus.COMPLETED) {
            throw new OnboardingException(OnboardingErrorCode.ALREADY_COMPLETED_ONBOARDING);
        }

        // OAuth2 핸들러에서 설정한 UserType과 요청의 UserType이 일치하는지 확인
        if (user.getUserType() != request.getUserType()) {
            log.warn("UserType mismatch - stored: {}, requested: {}",
                    user.getUserType(), request.getUserType());
            throw new OnboardingException(OnboardingErrorCode.USER_TYPE_MISMATCH);
        }

        // 사용자 타입에 따라 온보딩 처리
        if (request.getUserType() == UserType.AUDIENCE) {
            completeAudienceOnboarding(user, request);
        } else if (request.getUserType() == UserType.ORGANIZER) {
            completeOrganizerOnboarding(user, request);
        } else {
            throw new OnboardingException(OnboardingErrorCode.INVALID_USER_TYPE);
        }

        return OnboardingResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .userType(user.getUserType())
                .registrationStatus(user.getRegistrationStatus())
                .message("온보딩이 완료되었습니다.")
                .organizerName(user instanceof Organizer o ? o.getOrganizerName() : null)
                .build();
    }


    private void completeAudienceOnboarding(User user, OnboardingRequest request) {
        log.info("Completing audience onboarding for user: {}", user.getEmail());

        // 닉네임 필수 체크
        if (request.getNickname() == null || request.getNickname().isBlank()) {
            throw new OnboardingException(OnboardingErrorCode.NICKNAME_REQUIRED);
        }

        // 닉네임 길이 검증
        String nickname = request.getNickname().trim();
        if (nickname.length() < 2 || nickname.length() > 12) {
            throw new OnboardingException(OnboardingErrorCode.NICKNAME_LENGTH_INVALID);
        }

        // User 온보딩 완료
        user.completeAudienceOnboarding(nickname);
        userRepository.save(user);

        log.info("Audience onboarding completed for user: {}, nickname: {}",
                user.getEmail(), user.getNickname());
    }


    private void completeOrganizerOnboarding(User user, OnboardingRequest request) {
        log.info("Completing organizer onboarding for user: {}", user.getEmail());

        // 주최사명 필수 체크
        if (request.getOrganizerName() == null || request.getOrganizerName().isBlank()) {
            throw new OnboardingException(OnboardingErrorCode.ORGANIZER_NAME_REQUIRED);
        }

        // 주최사명 길이 검증
        String organizerName = request.getOrganizerName().trim();
        if (organizerName.length() < 2 || organizerName.length() > 12) {
            throw new OnboardingException(OnboardingErrorCode.ORGANIZER_NAME_LENGTH_INVALID);
        }

        // 주최사명 중복 체크
        validateOrganizerNameUniqueness(organizerName);

        ((Organizer) user).completeOrganizerOnboarding(organizerName);
        userRepository.save(user);

        log.info("Organizer onboarding completed for user: {}, organizer: {}",
                user.getEmail(), organizerName);
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));

        return OnboardingStatusResponse.builder()
                .email(user.getEmail())
                .registrationStatus(user.getRegistrationStatus())
                .userType(user.getUserType())
                .needsOnboarding(user.getRegistrationStatus() == RegistrationStatus.PENDING)
                .build();
    }

    private void validateOrganizerNameUniqueness(String organizerName) {
        if (organizerRepository.existsByOrganizerName(organizerName)) {
            throw new OnboardingException(OnboardingErrorCode.DUPLICATE_ORGANIZER_NAME);
        }
    }
}
