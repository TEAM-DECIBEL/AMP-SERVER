package com.amp.global.security.service;


import com.amp.domain.user.AuthProvider;
import com.amp.domain.user.Role;
import com.amp.domain.user.User;
import com.amp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuthUserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        return processOAuth2User(oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("username");
        String profile_image_url = oAuth2User.getAttribute("profile_image_url");
        String providerId = oAuth2User.getAttribute("sub");

        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, username, profile_image_url,providerId))
                .orElseGet(() -> createNewUser(email, username, profile_image_url, providerId));

        return oAuth2User;
    }

    private User createNewUser(String email, String username, String profile_image_url, String providerId) {
        User user = User.builder()
                .email(email)
                .username(username)
                .profile_image_url(profile_image_url)
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User user, String username, String profile_image_url,String providerId) {
        user.updateExistingUser(username, profile_image_url, providerId);
        return userRepository.save(user);
    } //여기 테스트 한번 해봐야할거 같아요
}
