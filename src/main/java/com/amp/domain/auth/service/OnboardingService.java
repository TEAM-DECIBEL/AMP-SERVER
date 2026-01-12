package com.amp.domain.auth.service;

import com.amp.domain.auth.dto.OnboardingRequest;
import com.amp.domain.auth.dto.OnboardingResponse;
import com.amp.domain.auth.dto.OnboardingStatusResponse;
import com.amp.domain.user.entity.RegistrationStatus;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import com.amp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final UserRepository userRepository;

    @Transactional
    public OnboardingResponse completeOnboarding(String email, OnboardingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (user.getRegistrationStatus() == RegistrationStatus.COMPLETED) {
            throw new IllegalStateException("이미 온보딩이 완료된 사용자입니다."); //todo 커스텀 예외처리
        }

        user.completeOnboarding(request.getUserType(), request.getName());
        userRepository.save(user);

        log.info("Onboarding completed - email: {}, type: {}, name: {}",
                email, request.getUserType(), request.getName());

        return OnboardingResponse.builder()
                .email(user.getEmail())
                .userType(user.getUserType())
                .name(request.getUserType().equals(UserType.ORGANIZER)
                        ? user.getOrganizerName()
                        : user.getNickname())
                .status(user.getRegistrationStatus())
                .message("온보딩이 완료되었습니다.")
                .build();
    }

    @Transactional(readOnly = true)
    public OnboardingStatusResponse getOnboardingStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return OnboardingStatusResponse.builder()
                .status(user.getRegistrationStatus())
                .isCompleted(user.isOnboardingCompleted())
                .build();
    }

}