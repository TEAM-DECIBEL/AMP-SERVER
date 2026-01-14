package com.amp.global.security.service;

import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(UserErrorCode.USER_NOT_AUTHENTICATED);
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
    }
}
