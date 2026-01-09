package com.amp.global.security.service;


import com.amp.domain.user.entity.AuthProvider;
import com.amp.domain.user.entity.Role;
import com.amp.domain.user.entity.User;
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
        String nickname = oAuth2User.getAttribute("nickname");
        String profile_image_url = oAuth2User.getAttribute("profile_image_url");
        String providerId = oAuth2User.getAttribute("sub");

        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateExistingUser(existingUser, nickname, profile_image_url,providerId))
                .orElseGet(() -> createNewUser(email, nickname, profile_image_url, providerId));

        return oAuth2User;
    }

    private User createNewUser(String email, String nickname, String profileImageUrl, String providerId) {
        User user = User.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .provider(AuthProvider.GOOGLE)
                .providerId(providerId)
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    private User updateExistingUser(User user, String username, String profileImageUrl,String providerId) {
        user.updateExistingUser(username, profileImageUrl, providerId);
        return userRepository.save(user);
    } //여기 테스트 한번 해봐야할거 같아요
}
