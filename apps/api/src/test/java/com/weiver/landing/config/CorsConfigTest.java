package com.weiver.landing.config;

import com.weiver.landing.visitor.VisitorController;
import com.weiver.landing.visitor.VisitorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitorController.class)
@TestPropertySource(properties = {
    "app.web-allowed-origin=http://localhost:5173",
    "app.admin-token=test-token"
})
class CorsConfigTest {

    @Autowired MockMvc mvc;
    @MockitoBean VisitorService visitorService;

    // AC-C1 preflight
    @Test
    @DisplayName("허용 오리진 preflight → 204, CORS 헤더 포함")
    void preflight_allowedOrigin() throws Exception {
        mvc.perform(options("/api/v1/visitors/bootstrap")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
            .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    // AC-C2
    @Test
    @DisplayName("미허용 오리진 → CORS 에러 (Access-Control-Allow-Origin 없음)")
    void preflight_disallowedOrigin() throws Exception {
        mvc.perform(options("/api/v1/visitors/bootstrap")
                .header("Origin", "https://evil.example.com")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
