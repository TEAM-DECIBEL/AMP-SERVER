package com.amp.global.security.handler;

import com.amp.global.common.CommonErrorCode;
import com.amp.global.response.error.ErrorResponseUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.error("Access denied: {} - URI: {}",
                accessDeniedException.getMessage(),
                request.getRequestURI());

        ErrorResponseUtil.writeErrorResponse(
                response,
                CommonErrorCode.FORBIDDEN,
                request.getRequestURI()
        );
    }
}