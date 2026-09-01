package com.sunrise.dental.audit;

import com.sunrise.dental.dto.response.AuditLogResponse;
import com.sunrise.dental.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Service contract for the audit trail (a functional requirement to track
 * who did what and when - supports accountability at the clinic).
 */
public interface AuditService {

    void log(String action, String entityType, Long entityId, String details);

    PageResponse<AuditLogResponse> getAll(Pageable pageable);

    PageResponse<AuditLogResponse> getByUsername(String username, Pageable pageable);

    PageResponse<AuditLogResponse> search(String action, Pageable pageable);

    long countSince(java.time.LocalDateTime since);
}
