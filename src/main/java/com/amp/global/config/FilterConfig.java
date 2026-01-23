package com.amp.global.config;

import com.amp.global.filter.SimpleCorsFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Slf4j
@Configuration
public class FilterConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public FilterRegistrationBean<SimpleCorsFilter> simpleCorsFilter() {
        log.info("🔧 Registering SimpleCorsFilter");

        FilterRegistrationBean<SimpleCorsFilter> registrationBean = new FilterRegistrationBean<>();

        SimpleCorsFilter corsFilter = new SimpleCorsFilter(allowedOrigins);
        registrationBean.setFilter(corsFilter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registrationBean.setName("SimpleCorsFilter");

        log.info("✅ SimpleCorsFilter registered");

        return registrationBean;
    }
}