package com.sunrise.dental.controller;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.AuditLogResponse;
import com.sunrise.dental.dto.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getAll(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @PageableDefault(sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        if (username != null && !username.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved",
                    auditService.getByUsername(username, pageable)));
        }
        if (action != null && !action.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", auditService.search(action, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved", auditService.getAll(pageable)));
    }
}
