package com.weiver.landing.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiver.landing.admin.AdminAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AdminAuthFilter> adminAuthFilter(
            @Value("${app.admin-token}") String adminToken,
            ObjectMapper objectMapper) {
        AdminAuthFilter filter = new AdminAuthFilter(adminToken, objectMapper);
        FilterRegistrationBean<AdminAuthFilter> reg = new FilterRegistrationBean<>(filter);
        reg.addUrlPatterns("/api/v1/admin/*");
        reg.setOrder(1);
        return reg;
    }
}
