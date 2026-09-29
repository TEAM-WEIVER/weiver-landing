package com.weiver.landing.event;

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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@org.springframework.test.context.TestPropertySource(properties = "app.admin-token=test-token")
class EventControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean EventService eventService;

    // AC-2-1
    @Test
    @DisplayName("유효한 이벤트 요청 → 200, tracking_events INSERT")
    void trackEvent_valid() throws Exception {
        doNothing().when(eventService).track(any());

        mvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest("page_view"))))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("섹션 노출 이벤트와 properties → 200")
    void trackEvent_sectionView_withProperties() throws Exception {
        Map<String, Object> req = Map.of(
            "visitorId", UUID.randomUUID().toString(),
            "sessionId", UUID.randomUUID().toString(),
            "eventName", "section_view",
            "path", "/",
            "properties", Map.of("sectionId", "why", "threshold", "50")
        );

        mvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk());
    }

    // AC-2-2
    @Test
    @DisplayName("DB 오류 발생해도 200 반환 (fire-and-forget)")
    void trackEvent_dbFailure_still200() throws Exception {
        doThrow(new RuntimeException("DB down")).when(eventService).track(any());

        mvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest("page_view"))))
            .andExpect(status().isOk());
    }

    // AC-2-3
    @Test
    @DisplayName("허용되지 않는 eventName → 400")
    void trackEvent_invalidEventName() throws Exception {
        Map<String, Object> req = Map.of(
            "visitorId", UUID.randomUUID().toString(),
            "sessionId", UUID.randomUUID().toString(),
            "eventName", "invalid_event",
            "path", "/"
        );

        mvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    // AC-2-4
    @Test
    @DisplayName("필수 필드 누락 → 400")
    void trackEvent_missingField() throws Exception {
        Map<String, Object> req = Map.of(
            "visitorId", UUID.randomUUID().toString(),
            "eventName", "page_view"
            // sessionId, path 누락
        );

        mvc.perform(post("/api/v1/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    private Map<String, Object> validRequest(String eventName) {
        return Map.of(
            "visitorId", UUID.randomUUID().toString(),
            "sessionId", UUID.randomUUID().toString(),
            "eventName", eventName,
            "path", "/"
        );
    }
}
