package com.amp.domain.audience.service;

import com.amp.domain.audience.dto.response.AudienceMyPageResponse;
import com.amp.domain.user.entity.Audience;
import com.amp.domain.user.repository.AudienceRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.amp.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudienceService {

    private final AudienceRepository audienceRepository;

    public AudienceMyPageResponse getMyPage(String email) {
        Audience audience = audienceRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return AudienceMyPageResponse.from(audience);
    }
}
