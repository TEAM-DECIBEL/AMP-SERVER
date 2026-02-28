package com.amp.global.security.service;

import com.amp.domain.user.entity.Audience;
import com.amp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("비로그인 유저는 '관객' 반환")
    void getDisplayNicknameAnonymous() {
        SecurityContextHolder.clearContext();
        assertThat(authService.getDisplayNickname()).isEqualTo("관객");
    }

    @Test
    @DisplayName("로그인한 Audience 유저는 닉네임 반환")
    void getDisplayNicknameLoggedInAudience() {
        Audience audience = Audience.builder().nickname("앰프").build();

        Authentication auth = mock(Authentication.class);
        given(auth.isAuthenticated()).willReturn(true);
        given(auth.getName()).willReturn("amp@amp.com");
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(userRepository.findByEmail("amp@amp.com")).willReturn(Optional.of(audience));

        assertThat(authService.getDisplayNickname()).isEqualTo("앰프");
    }

}