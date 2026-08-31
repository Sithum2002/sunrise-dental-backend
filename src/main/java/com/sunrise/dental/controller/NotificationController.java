package com.sunrise.dental.controller;

import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.NotificationResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getAll(
            @RequestParam(required = false) String recipient, Pageable pageable) {
        if (recipient != null && !recipient.isBlank()) {
            return ResponseEntity.ok(ApiResponse.success("Notifications retrieved",
                    notificationService.getByRecipient(recipient, pageable)));
        }
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notificationService.getAll(pageable)));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }
}
