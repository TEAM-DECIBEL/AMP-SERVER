package com.amp.global.security;


import com.amp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OAuth2FlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("OAuth2 인증 요청 - localhost:5173에서 시작")
    @WithMockUser
    void testOAuth2AuthorizationRequestFromAudienceDomain() throws Exception {
        // When & Then
        mockMvc.perform(get("/oauth2/authorization/google")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().is3xxRedirection());

        // Google OAuth2 페이지로 리다이렉트되는지 확인
        // state에 userType=AUDIENCE가 포함되어 있는지는 로그로 확인
    }

    @Test
    @DisplayName("OAuth2 인증 요청 - localhost:5174에서 시작")
    @WithMockUser
    void testOAuth2AuthorizationRequestFromOrganizerDomain() throws Exception {
        // When & Then
        mockMvc.perform(get("/oauth2/authorization/google")
                        .header("Origin", "http://localhost:5174"))
                .andExpect(status().is3xxRedirection());
    }
}
