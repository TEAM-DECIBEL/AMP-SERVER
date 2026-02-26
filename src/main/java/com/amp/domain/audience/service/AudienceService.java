package com.amp.domain.audience.service;

import com.amp.domain.audience.dto.response.AudienceMyPageResponse;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.amp.domain.user.exception.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AudienceService {

    private final UserRepository userRepository;

    public AudienceMyPageResponse getMyPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return AudienceMyPageResponse.from(user);
    }
}
