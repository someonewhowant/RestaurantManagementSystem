package com.vanilla.crm.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Health check endpoint for verifying that the API is running.
 */
@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Vanilla CRM API",
                "timestamp", Instant.now().toString(),
                "version", "0.0.1-SNAPSHOT"
        ));
    }
}
