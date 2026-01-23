package com.amp.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class SimpleCorsFilter implements Filter {

    private final List<String> allowedOrigins;

    public SimpleCorsFilter(String allowedOrigins) {
        this.allowedOrigins = Arrays.asList(allowedOrigins.split(","));
        log.info("🔧 SimpleCorsFilter initialized with origins: {}", this.allowedOrigins);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String origin = request.getHeader("Origin");
        String method = request.getMethod();
        String uri = request.getRequestURI();

        log.info("🌐 CORS Filter - Method: {}, Origin: {}, URI: {}", method, origin, uri);

        // Origin이 있으면 CORS 헤더 추가
        if (origin != null) {
            boolean isAllowed = allowedOrigins.stream()
                    .map(String::trim)
                    .anyMatch(allowedOrigin -> allowedOrigin.equals(origin));

            if (isAllowed) {
                log.info("✅ Origin allowed: {}", origin);

                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With, Accept, Origin");
                response.setHeader("Access-Control-Max-Age", "3600");
                response.setHeader("Access-Control-Expose-Headers", "Authorization, Set-Cookie");

                log.info("✅ CORS headers added");
            } else {
                log.warn("❌ Origin not allowed: {} (allowed: {})", origin, allowedOrigins);
            }
        }

        // OPTIONS 요청은 바로 응답
        if ("OPTIONS".equalsIgnoreCase(method)) {
            log.info("⚡ OPTIONS request handled - returning 200");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }
}