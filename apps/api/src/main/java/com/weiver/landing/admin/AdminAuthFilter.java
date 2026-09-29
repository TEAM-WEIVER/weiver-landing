package com.weiver.landing.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiver.landing.common.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AdminAuthFilter extends OncePerRequestFilter {

    private final String adminToken;
    private final ObjectMapper objectMapper;

    public AdminAuthFilter(@Value("${app.admin-token}") String adminToken, ObjectMapper objectMapper) {
        this.adminToken = adminToken;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        String expected = "Bearer " + adminToken;

        if (!expected.equals(header)) {
            res.setStatus(HttpStatus.UNAUTHORIZED.value());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(res.getWriter(), ErrorResponse.of("UNAUTHORIZED"));
            return;
        }
        chain.doFilter(req, res);
    }
}
