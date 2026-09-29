package com.weiver.landing.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class EventRepository {

    private final JdbcClient jdbc;
    private final ObjectMapper objectMapper;

    public EventRepository(JdbcClient jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    public void insert(TrackEventRequest req) {
        jdbc.sql("""
            INSERT INTO tracking_events (visitor_id, session_id, event_name, path, properties)
            VALUES (:visitorId, :sessionId, :eventName, :path, :properties::jsonb)
            """)
            .param("visitorId", req.visitorId())
            .param("sessionId", req.sessionId())
            .param("eventName", req.eventName())
            .param("path", req.path())
            .param("properties", toJson(req.properties()))
            .update();
    }

    private String toJson(Map<String, String> properties) {
        try {
            return objectMapper.writeValueAsString(properties == null ? Map.of() : properties);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("이벤트 properties를 저장할 수 없습니다", e);
        }
    }
}
