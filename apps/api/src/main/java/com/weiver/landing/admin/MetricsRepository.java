package com.weiver.landing.admin;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricsRepository {

    private final JdbcClient jdbc;

    public MetricsRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<DailyFunnelRow> findAll() {
        return jdbc.sql("""
            SELECT day, unique_visitors, sessions,
                   reserving_visitors, reservations, visitor_conversion_rate
            FROM daily_landing_funnel
            ORDER BY day DESC
            """)
            .query((rs, _) -> new DailyFunnelRow(
                rs.getDate("day").toLocalDate(),
                rs.getLong("unique_visitors"),
                rs.getLong("sessions"),
                rs.getLong("reserving_visitors"),
                rs.getLong("reservations"),
                rs.getBigDecimal("visitor_conversion_rate")
            ))
            .list();
    }
}
