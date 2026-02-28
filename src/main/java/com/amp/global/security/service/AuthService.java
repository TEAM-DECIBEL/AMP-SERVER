package com.amp.global.security.service;

import com.amp.domain.user.entity.Audience;
import com.amp.domain.user.entity.User;
import com.amp.domain.user.exception.UserErrorCode;
import com.amp.domain.user.repository.UserRepository;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

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

    public String getDisplayNickname() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isLoggedInUser(authentication)) {
            return "관객";
        }
        User user = getCurrentUserOrNull();
        if (!(user instanceof Audience audience)) {
            return "관객";
        }
        return audience.getNickname() != null ? audience.getNickname() : "관객";
    }

    public boolean isLoggedInUser(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !(authentication instanceof AnonymousAuthenticationToken);
    }

    public User getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (CustomException e) {
            return null;
        }
    }
}
