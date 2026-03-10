package com.amp.domain.auth.service;

import com.amp.domain.auth.dto.OrganizerRegistrationStatusResponse;
import com.amp.domain.auth.dto.VerifyRegistrationCodeRequest;
import com.amp.domain.auth.dto.VerifyRegistrationCodeResponse;
import com.amp.domain.auth.entity.OrganizerRegistration;
import com.amp.domain.auth.exception.OnboardingErrorCode;
import com.amp.domain.auth.exception.OnboardingException;
import com.amp.domain.auth.exception.RegistrationErrorCode;
import com.amp.domain.auth.repository.OrganizerRegistrationRepository;
import com.amp.domain.user.entity.Organizer;
import com.amp.domain.user.entity.User;
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
public class OrganizerRegistrationService {

    private static final int MAX_ATTEMPTS = 5;

    private final OrganizerRegistrationRepository organizerRegistrationRepository;
    private final OrganizerRepository organizerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean isEmailRegistered(String email) {
        return organizerRegistrationRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public OrganizerRegistrationStatusResponse getRegistrationStatus(String email) {
        return organizerRegistrationRepository.findByEmail(email)
                .map(registration -> {
                    if (registration.isVerified()) {
                        return OrganizerRegistrationStatusResponse.alreadyVerified();
                    }
                    return OrganizerRegistrationStatusResponse.codeRequired();
                })
                .orElse(OrganizerRegistrationStatusResponse.notRegistered());
    }

    public VerifyRegistrationCodeResponse verifyRegistrationCode(String email, VerifyRegistrationCodeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(CommonErrorCode.USER_NOT_FOUND));

        OrganizerRegistration registration = organizerRegistrationRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(RegistrationErrorCode.EMAIL_NOT_REGISTERED));

        // 이미 검증 완료된 경우
        if (registration.isVerified()) {
            throw new CustomException(RegistrationErrorCode.ALREADY_VERIFIED);
        }

        // 최대 시도 횟수 초과 확인
        if (registration.isMaxAttemptsExceeded(MAX_ATTEMPTS)) {
            throw new CustomException(RegistrationErrorCode.MAX_ATTEMPTS_EXCEEDED);
        }

        // 시도 횟수 증가
        registration.incrementAttemptCount();
        organizerRegistrationRepository.save(registration);

        // 가입코드 검증
        if (!registration.getRegistrationCode().equals(request.getRegistrationCode())) {
            int remainingAttempts = MAX_ATTEMPTS - registration.getAttemptCount();
            log.warn("Invalid registration code attempt for email: {}, remaining attempts: {}",
                    email, remainingAttempts);

            if (remainingAttempts <= 0) {
                throw new CustomException(RegistrationErrorCode.MAX_ATTEMPTS_EXCEEDED);
            }

            return VerifyRegistrationCodeResponse.failure(remainingAttempts);
        }

        // 주최사명 중복 체크
        String organizerName = request.getOrganizerName().trim();
        validateOrganizerNameUniqueness(organizerName);

        // 검증 성공
        registration.markAsVerified();
        organizerRegistrationRepository.save(registration);

        // Organizer로 캐스팅하여 온보딩 완료 처리
        Organizer organizer = (Organizer) user;
        organizer.completeOrganizerOnboarding(organizerName);
        userRepository.save(organizer);

        log.info("Registration verified and onboarding completed for email: {}, organizerName: {}",
                email, organizerName);

        return VerifyRegistrationCodeResponse.success(organizerName);
    }

    private void validateOrganizerNameUniqueness(String organizerName) {
        if (organizerRepository.existsByOrganizerName(organizerName)) {
            throw new OnboardingException(OnboardingErrorCode.DUPLICATE_ORGANIZER_NAME);
        }
    }
}
