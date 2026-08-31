package com.sunrise.dental.dto.response;

import com.sunrise.dental.enums.NotificationChannel;
import com.sunrise.dental.enums.NotificationStatus;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String recipient,
        NotificationChannel channel,
        String subject,
        String content,
        NotificationStatus status,
        LocalDateTime sentAt,
        LocalDateTime readAt,
        String errorMessage
) {
}
