package com.sunrise.dental.audit;

import com.sunrise.dental.dto.response.AuditLogResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.repository.UserRepository;
import com.sunrise.dental.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists audit entries for every significant action.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void log(String action, String entityType, Long entityId, String details) {
        try {
            String username = SecurityUtils.currentUsername();
            String ip = SecurityUtils.currentIpAddress();
            AuditLog entry = AuditLog.builder()
                    .username(username)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details == null || details.length() <= 1000 ? details : details.substring(0, 1000))
                    .ipAddress(ip)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();
            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to write audit log for action {}: {}", action, ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAll(Pageable pageable) {
        return toResponse(auditLogRepository.findAll(pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getByUsername(String username, Pageable pageable) {
        return toResponse(auditLogRepository.findByUsernameOrderByTimestampDesc(username, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(String action, Pageable pageable) {
        return toResponse(auditLogRepository.findByActionContainingIgnoreCaseOrderByTimestampDesc(action, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public long countSince(java.time.LocalDateTime since) {
        return auditLogRepository.countByTimestampAfter(since);
    }

    private PageResponse<AuditLogResponse> toResponse(Page<AuditLog> page) {
        return new PageResponse<>(
                page.getContent().stream()
                        .map(a -> new AuditLogResponse(a.getId(), a.getUsername(), a.getAction(),
                                a.getEntityType(), a.getEntityId(), a.getDetails(),
                                a.getIpAddress(), a.getTimestamp()))
                        .toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
