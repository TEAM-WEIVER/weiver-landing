package com.weiver.landing.admin;

import com.weiver.landing.common.ErrorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class MetricsController {

    private final MetricsRepository metricsRepository;
    private final String adminToken;

    public MetricsController(MetricsRepository metricsRepository,
                             @Value("${app.admin-token}") String adminToken) {
        this.metricsRepository = metricsRepository;
        this.adminToken = adminToken;
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!("Bearer " + adminToken).equals(auth)) {
            return ResponseEntity.status(401).body(ErrorResponse.of("UNAUTHORIZED"));
        }
        return ResponseEntity.ok(metricsRepository.findAll());
    }
}
