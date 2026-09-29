package com.weiver.landing.visitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class VisitorService {

    private final VisitorRepository visitorRepo;
    private final SessionRepository sessionRepo;
    private final ObjectMapper objectMapper;

    public VisitorService(VisitorRepository visitorRepo, SessionRepository sessionRepo, ObjectMapper objectMapper) {
        this.visitorRepo = visitorRepo;
        this.sessionRepo = sessionRepo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UUID bootstrap(UUID cookieVisitorId, BootstrapRequest req) {
        UUID visitorId;

        if (cookieVisitorId != null && visitorRepo.exists(cookieVisitorId)) {
            // AC-1-2: 재방문자
            visitorId = cookieVisitorId;
            visitorRepo.updateLastSeen(visitorId);
        } else {
            // AC-1-1, AC-1-3: 신규 또는 unknown cookie
            visitorId = UUID.randomUUID();
            visitorRepo.insert(visitorId, req.path(), toJson(req.utm()));
        }

        sessionRepo.insertIfAbsent(req.sessionId(), visitorId, req.path(), req.referrer(), toJson(req.utm()));
        return visitorId;
    }

    private String toJson(Map<String, String> utm) {
        if (utm == null) return "{}";
        try {
            return objectMapper.writeValueAsString(utm);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
