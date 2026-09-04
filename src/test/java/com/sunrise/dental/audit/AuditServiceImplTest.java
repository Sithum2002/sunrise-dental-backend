package com.sunrise.dental.audit;

import com.sunrise.dental.dto.response.AuditLogResponse;
import com.sunrise.dental.dto.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        auditLog = AuditLog.builder()
                .id(1L)
                .username("admin")
                .action("CREATE")
                .entityType("Patient")
                .entityId(1L)
                .details("Registered patient John Doe")
                .ipAddress("127.0.0.1")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("log()")
    class Log {

        @Test
        @DisplayName("persists an audit entry")
        void log_success() {
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

            auditService.log("CREATE", "Patient", 1L, "Registered patient");

            verify(auditLogRepository).save(argThat(entry ->
                    "CREATE".equals(entry.getAction())
                            && "Patient".equals(entry.getEntityType())
                            && entry.getEntityId() == 1L
                            && "Registered patient".equals(entry.getDetails())
                            && entry.getTimestamp() != null));
        }

        @Test
        @DisplayName("uses system user when no authentication context")
        void log_noAuthContext() {
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

            auditService.log("LOGIN", "User", 1L, "login");

            verify(auditLogRepository).save(argThat(entry -> entry.getUsername() != null));
        }

        @Test
        @DisplayName("truncates long details to 1000 characters")
        void log_truncatesLongDetails() {
            String longDetails = "x".repeat(5000);
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

            auditService.log("CREATE", "Entity", 1L, longDetails);

            verify(auditLogRepository).save(argThat(entry -> entry.getDetails().length() <= 1000));
        }

        @Test
        @DisplayName("does not throw when repository save fails")
        void log_repositoryFailure() {
            when(auditLogRepository.save(any(AuditLog.class)))
                    .thenThrow(new RuntimeException("DB down"));

            assertDoesNotThrow(() -> auditService.log("CREATE", "Entity", 1L, "details"));
        }

        @Test
        @DisplayName("handles null entityId and details")
        void log_nullParameters() {
            when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

            assertDoesNotThrow(() -> auditService.log("LOGOUT", "User", null, null));
            verify(auditLogRepository).save(argThat(entry -> entry.getEntityId() == null));
        }
    }

    @Nested
    @DisplayName("getAll() / getByUsername() / search() / countSince()")
    class QueryMethods {

        @Test
        @DisplayName("returns all audit logs")
        void getAll_success() {
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
            Pageable pageable = PageRequest.of(0, 10);
            when(auditLogRepository.findAll(pageable)).thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getAll(pageable);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals("CREATE", result.content().get(0).action());
            assertEquals("127.0.0.1", result.content().get(0).ipAddress());
        }

        @Test
        @DisplayName("returns empty audit logs when none exist")
        void getAll_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(auditLogRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

            PageResponse<AuditLogResponse> result = auditService.getAll(pageable);

            assertTrue(result.content().isEmpty());
        }

        @Test
        @DisplayName("returns audit logs by username")
        void getByUsername() {
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
            Pageable pageable = PageRequest.of(0, 10);
            when(auditLogRepository.findByUsernameOrderByTimestampDesc("admin", pageable))
                    .thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.getByUsername("admin", pageable);

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            verify(auditLogRepository).findByUsernameOrderByTimestampDesc("admin", pageable);
        }

        @Test
        @DisplayName("searches audit logs by action")
        void search() {
            Page<AuditLog> page = new PageImpl<>(List.of(auditLog));
            Pageable pageable = PageRequest.of(0, 10);
            when(auditLogRepository.findByActionContainingIgnoreCaseOrderByTimestampDesc("create", pageable))
                    .thenReturn(page);

            PageResponse<AuditLogResponse> result = auditService.search("create", pageable);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }

        @Test
        @DisplayName("counts entries since a time")
        void countSince() {
            LocalDateTime since = LocalDateTime.now().minusDays(1);
            when(auditLogRepository.countByTimestampAfter(since)).thenReturn(5L);

            long result = auditService.countSince(since);

            assertEquals(5L, result);
        }
    }
}
