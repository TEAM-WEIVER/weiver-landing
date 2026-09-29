package com.weiver.landing.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetricsController.class)
@TestPropertySource(properties = "app.admin-token=test-token")
class MetricsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean MetricsRepository metricsRepository;

    // AC-4-1
    @Test
    @DisplayName("유효한 Bearer 토큰 → 200, 날짜 내림차순 지표 반환")
    void getMetrics_authorized() throws Exception {
        DailyFunnelRow row = new DailyFunnelRow(
            LocalDate.of(2026, 9, 28), 120L, 145L, 18L, 18L, new BigDecimal("15.00")
        );
        when(metricsRepository.findAll()).thenReturn(List.of(row));

        mvc.perform(get("/api/v1/admin/metrics")
                .header("Authorization", "Bearer test-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].day").value("2026-09-28"))
            .andExpect(jsonPath("$[0].uniqueVisitors").value(120))
            .andExpect(jsonPath("$[0].sessions").value(145))
            .andExpect(jsonPath("$[0].reservingVisitors").value(18))
            .andExpect(jsonPath("$[0].reservations").value(18))
            .andExpect(jsonPath("$[0].visitorConversionRate").value(15.00));
    }

    // AC-4-2 (헤더 없음)
    @Test
    @DisplayName("Authorization 헤더 없음 → 401")
    void getMetrics_noToken() throws Exception {
        mvc.perform(get("/api/v1/admin/metrics"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    // AC-4-2 (토큰 불일치)
    @Test
    @DisplayName("토큰 불일치 → 401")
    void getMetrics_wrongToken() throws Exception {
        mvc.perform(get("/api/v1/admin/metrics")
                .header("Authorization", "Bearer wrong-token"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }
}
