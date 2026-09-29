package com.weiver.landing.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
@org.springframework.test.context.TestPropertySource(properties = "app.admin-token=test-token")
class ReservationControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ReservationService reservationService;

    // AC-3-1
    @Test
    @DisplayName("유효한 예약 요청 → 201, 생성된 id 반환")
    void reserve_valid() throws Exception {
        UUID createdId = UUID.randomUUID();
        when(reservationService.reserve(any())).thenReturn(createdId);

        mvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(createdId.toString()));
    }

    // AC-3-2
    @Test
    @DisplayName("중복 이메일 → 409 DUPLICATE_EMAIL")
    void reserve_duplicateEmail() throws Exception {
        when(reservationService.reserve(any()))
            .thenThrow(new DuplicateEmailException());

        mvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest())))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("DUPLICATE_EMAIL"));
    }

    // AC-3-3
    @Test
    @DisplayName("privacyConsentAt 누락 → 400")
    void reserve_missingPrivacyConsent() throws Exception {
        Map<String, Object> req = Map.of(
            "visitorId", UUID.randomUUID().toString(),
            "sessionId", UUID.randomUUID().toString(),
            "name", "홍길동",
            "email", "test@example.com",
            "reservationType", "QUICK_AI_INTERVIEW"
            // privacyConsentAt 누락
        );

        mvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    // AC-3-4
    @Test
    @DisplayName("이메일 형식 불일치 → 400")
    void reserve_invalidEmail() throws Exception {
        Map<String, Object> req = validRequest();
        Map<String, Object> modified = new java.util.HashMap<>(req);
        modified.put("email", "not-an-email");

        mvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modified)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    // AC-3-5
    @Test
    @DisplayName("허용되지 않는 reservationType → 400")
    void reserve_invalidType() throws Exception {
        Map<String, Object> req = new java.util.HashMap<>(validRequest());
        req.put("reservationType", "UNKNOWN_TYPE");

        mvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    // AC-3-6
    @Test
    @DisplayName("utm 필드 전체 생략 → 201 (optional)")
    void reserve_withoutUtm() throws Exception {
        UUID createdId = UUID.randomUUID();
        when(reservationService.reserve(any())).thenReturn(createdId);

        Map<String, Object> req = new java.util.HashMap<>(validRequest());
        req.remove("utm");

        mvc.perform(post("/api/v1/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }

    private Map<String, Object> validRequest() {
        Map<String, Object> req = new java.util.HashMap<>();
        req.put("visitorId", UUID.randomUUID().toString());
        req.put("sessionId", UUID.randomUUID().toString());
        req.put("name", "홍길동");
        req.put("email", "test@example.com");
        req.put("reservationType", "QUICK_AI_INTERVIEW");
        req.put("privacyConsentAt", Instant.now().toString());
        req.put("utm", Map.of());
        return req;
    }
}
