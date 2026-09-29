package com.weiver.landing.visitor;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class SessionRepository {

    private final JdbcClient jdbc;

    public SessionRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void insertIfAbsent(UUID sessionId, UUID visitorId, String path, String referrer, String utmJson) {
        jdbc.sql("""
            INSERT INTO landing_sessions (id, visitor_id, landing_path, referrer, utm)
            VALUES (:id, :visitorId, :path, :referrer, :utm::jsonb)
            ON CONFLICT (id) DO NOTHING
            """)
            .param("id", sessionId)
            .param("visitorId", visitorId)
            .param("path", path)
            .param("referrer", referrer)
            .param("utm", utmJson)
            .update();
    }
}
