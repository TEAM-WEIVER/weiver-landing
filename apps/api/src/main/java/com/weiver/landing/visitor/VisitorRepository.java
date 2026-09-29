package com.weiver.landing.visitor;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class VisitorRepository {

    private final JdbcClient jdbc;

    public VisitorRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public boolean exists(UUID id) {
        return jdbc.sql("SELECT COUNT(1) FROM visitors WHERE id = :id")
            .param("id", id)
            .query(Integer.class)
            .single() > 0;
    }

    public void insert(UUID id, String firstPath, String utmJson) {
        jdbc.sql("""
            INSERT INTO visitors (id, first_landing_path, first_utm)
            VALUES (:id, :path, :utm::jsonb)
            """)
            .param("id", id)
            .param("path", firstPath)
            .param("utm", utmJson)
            .update();
    }

    public void updateLastSeen(UUID id) {
        jdbc.sql("UPDATE visitors SET last_seen_at = now() WHERE id = :id")
            .param("id", id)
            .update();
    }
}
