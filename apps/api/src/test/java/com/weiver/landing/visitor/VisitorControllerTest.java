package com.weiver.landing.visitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitorController.class)
@org.springframework.test.context.TestPropertySource(properties = "app.admin-token=test-token")
class VisitorControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean VisitorService visitorService;

    // AC-1-1
    @Test
    @DisplayName("쿠키 없는 신규 방문자 → 새 UUID 발급, visitor + session INSERT")
    void bootstrap_newVisitor() throws Exception {
        UUID newId = UUID.randomUUID();
        when(visitorService.bootstrap(any(), any())).thenReturn(newId);

        mvc.perform(post("/api/v1/visitors/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
            .andExpect(status().isOk())
            .andExpect(cookie().exists("visitor_id"))
            .andExpect(cookie().httpOnly("visitor_id", true))
            .andExpect(cookie().maxAge("visitor_id", 15_552_000))
            .andExpect(jsonPath("$.visitorId").value(newId.toString()));
    }

    // AC-1-2
    @Test
    @DisplayName("유효한 visitor_id 쿠키 → 기존 UUID 반환, last_seen_at 갱신")
    void bootstrap_returningVisitor() throws Exception {
        UUID existingId = UUID.randomUUID();
        when(visitorService.bootstrap(any(), any())).thenReturn(existingId);

        mvc.perform(post("/api/v1/visitors/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest()))
                .cookie(new jakarta.servlet.http.Cookie("visitor_id", existingId.toString())))
            .andExpect(status().isOk())
            .andExpect(cookie().value("visitor_id", existingId.toString()))
            .andExpect(jsonPath("$.visitorId").value(existingId.toString()));
    }

    // AC-1-3
    @Test
    @DisplayName("DB에 없는 visitor_id 쿠키 → 새 UUID 발급 (silent recovery)")
    void bootstrap_unknownCookie() throws Exception {
        UUID unknownId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();
        when(visitorService.bootstrap(any(), any())).thenReturn(newId);

        mvc.perform(post("/api/v1/visitors/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest()))
                .cookie(new jakarta.servlet.http.Cookie("visitor_id", unknownId.toString())))
            .andExpect(status().isOk())
            .andExpect(cookie().value("visitor_id", newId.toString()))
            .andExpect(jsonPath("$.visitorId").value(newId.toString()));
    }

    private Map<String, Object> validRequest() {
        return Map.of(
            "sessionId", UUID.randomUUID().toString(),
            "path", "/",
            "utm", Map.of()
        );
    }
}
