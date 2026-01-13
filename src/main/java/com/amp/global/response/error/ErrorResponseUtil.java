package com.amp.global.response.error;

import com.amp.global.common.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.IOException;

@Slf4j
public class ErrorResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());  // LocalDateTime 직렬화 시켜준대요

    public static void writeErrorResponse(
            HttpServletResponse response,
            ErrorCode errorCode,
            String path
    ) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        BaseErrorResponse errorResponse = BaseErrorResponse.of(errorCode);

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

        log.error("Security error response: {} - {}", errorCode.getCode(), path);
    }
}