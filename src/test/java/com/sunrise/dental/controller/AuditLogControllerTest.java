package com.sunrise.dental.controller;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.response.AuditLogResponse;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditLogController auditLogController;

    private AuditLogResponse auditLogResponse() {
        return new AuditLogResponse(
                1L, "admin", "CREATE", "Patient", 1L,
                "Registered patient", "127.0.0.1", LocalDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/audit-logs")
    class GetAll {

        @Test
        @DisplayName("returns all audit logs when no filters")
        void getAll_noFilters() {
            PageResponse<AuditLogResponse> pageResponse = new PageResponse<>(
                    List.of(auditLogResponse()), 0, 10, 1, 1);
            Pageable pageable = PageRequest.of(0, 10);
            when(auditService.getAll(pageable)).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> result =
                    auditLogController.getAll(null, null, pageable);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().content().size());
            verify(auditService).getAll(pageable);
            verify(auditService, never()).getByUsername(any(), any());
            verify(auditService, never()).search(any(), any());
        }

        @Test
        @DisplayName("returns audit logs filtered by username")
        void getAll_byUsername() {
            PageResponse<AuditLogResponse> pageResponse = new PageResponse<>(
                    List.of(auditLogResponse()), 0, 10, 1, 1);
            Pageable pageable = PageRequest.of(0, 10);
            when(auditService.getByUsername("admin", pageable)).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> result =
                    auditLogController.getAll("admin", null, pageable);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(auditService).getByUsername("admin", pageable);
        }

        @Test
        @DisplayName("returns audit logs filtered by action")
        void getAll_byAction() {
            PageResponse<AuditLogResponse> pageResponse = new PageResponse<>(
                    List.of(auditLogResponse()), 0, 10, 1, 1);
            Pageable pageable = PageRequest.of(0, 10);
            when(auditService.search("CREATE", pageable)).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> result =
                    auditLogController.getAll(null, "CREATE", pageable);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(auditService).search("CREATE", pageable);
        }

        @Test
        @DisplayName("returns empty when no audit logs")
        void getAll_empty() {
            PageResponse<AuditLogResponse> pageResponse = new PageResponse<>(
                    Collections.emptyList(), 0, 10, 0, 0);
            Pageable pageable = PageRequest.of(0, 10);
            when(auditService.getAll(pageable)).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> result =
                    auditLogController.getAll(null, null, pageable);

            assertTrue(result.getBody().getData().content().isEmpty());
        }
    }
}
