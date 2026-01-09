package com.amp.global.aop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoggingAspectTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/test/success"))
                .andExpect(status().isOk());
    }

    @Test
    void testError() throws Exception {
        mockMvc.perform(get("/api/v1/test/error"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testSlow() throws Exception {
        mockMvc.perform(get("/api/v1/test/slow"))
                .andExpect(status().isOk());
    }

}
