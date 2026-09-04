package com.sunrise.dental.controller;

import com.sunrise.dental.dto.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HealthControllerTest {

    private final HealthController healthController = new HealthController();

    @Nested
    @DisplayName("GET /api/health")
    class Health {

        @Test
        @DisplayName("returns service is UP")
        void health_success() {
            ResponseEntity<ApiResponse<Map<String, String>>> result = healthController.health();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Service is running", result.getBody().getMessage());
            assertEquals("UP", result.getBody().getData().get("status"));
            assertEquals("sunrise-dental-backend", result.getBody().getData().get("service"));
        }
    }
}
