package com.sunrise.dental.dto.response;

public record AuditLogResponse(
        Long id,
        String username,
        String action,
        String entityType,
        Long entityId,
        String details,
        String ipAddress,
        java.time.LocalDateTime timestamp
) {
}
