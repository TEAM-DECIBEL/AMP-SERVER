package com.amp.global.security.service;


import com.amp.domain.user.AuthProvider;
import com.amp.domain.user.Role;
import com.amp.domain.user.User;
import com.amp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuthUserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return processOAuth2User(oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String providerId = oAuth2User.getAttribute("sub");

        // 필수 값 검증
        if (email == null || email.trim().isEmpty()) {
            log.error("OAuth2 authentication failed: email is missing");
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        if (providerId == null || providerId.trim().isEmpty()) {
            log.error("OAuth2 authentication failed: provider ID is missing");
            throw new OAuth2AuthenticationException("Provider ID not found from OAuth2 provider");
        }

        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, name, picture))
                .orElseGet(() -> createNewUser(email, name, picture, providerId));

        return oAuth2User;
    }

    private User createNewUser(String email, String name, String picture, String providerId) {
        User user = User.builder()
                .email(email)
                .nickname(name)
                .profileImageUrl(picture)
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .role(Role.USER)
                .isActive(true)
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User user, String name, String picture) {
        user.updateExistingUser(name, picture);
        return userRepository.save(user);
    }
}

