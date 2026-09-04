package com.sunrise.dental.controller;

import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.NotificationResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.enums.NotificationChannel;
import com.sunrise.dental.enums.NotificationStatus;
import com.sunrise.dental.service.NotificationService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    @Nested
    @DisplayName("GET /api/notifications")
    class GetAll {

        @Test
        @DisplayName("returns all notifications when no recipient filter")
        void getAll_noRecipient() {
            PageResponse<NotificationResponse> pageResponse = new PageResponse<>(
                    Collections.emptyList(), 0, 10, 0, 0);
            when(notificationService.getAll(any(Pageable.class))).thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> result =
                    notificationController.getAll(null, PageRequest.of(0, 10));

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(notificationService).getAll(any(Pageable.class));
            verify(notificationService, never()).getByRecipient(any(), any());
        }

        @Test
        @DisplayName("returns notifications for recipient filter")
        void getAll_withRecipient() {
            PageResponse<NotificationResponse> pageResponse = new PageResponse<>(
                    List.of(new NotificationResponse(1L, "john@example.com",
                            NotificationChannel.EMAIL, "Subj", "Content",
                            NotificationStatus.SENT, LocalDateTime.now(), null, null)),
                    0, 10, 1, 1);
            when(notificationService.getByRecipient(eq("john@example.com"), any(Pageable.class)))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> result =
                    notificationController.getAll("john@example.com", PageRequest.of(0, 10));

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().content().size());
            verify(notificationService).getByRecipient(eq("john@example.com"), any(Pageable.class));
        }

        @Test
        @DisplayName("treats blank recipient as no filter")
        void getAll_blankRecipient() {
            PageResponse<NotificationResponse> pageResponse = new PageResponse<>(
                    Collections.emptyList(), 0, 10, 0, 0);
            when(notificationService.getAll(any(Pageable.class))).thenReturn(pageResponse);

            notificationController.getAll("   ", PageRequest.of(0, 10));

            verify(notificationService).getAll(any(Pageable.class));
            verify(notificationService, never()).getByRecipient(any(), any());
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/{id}/read")
    class MarkAsRead {

        @Test
        @DisplayName("marks notification as read")
        void markAsRead_success() {
            doNothing().when(notificationService).markAsRead(1L);

            ResponseEntity<ApiResponse<Void>> result = notificationController.markAsRead(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Notification marked as read", result.getBody().getMessage());
            verify(notificationService).markAsRead(1L);
        }
    }
}
