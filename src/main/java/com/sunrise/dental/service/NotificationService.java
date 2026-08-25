package com.sunrise.dental.service;

import com.sunrise.dental.dto.response.NotificationResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Bill;
import com.sunrise.dental.enums.NotificationChannel;
import org.springframework.data.domain.Pageable;

/**
 * Notification facade - unified API used by domain services to notify
 * patients/staff through email or SMS without knowing the transport details.
 */
public interface NotificationService {

    NotificationResponse send(NotificationChannel channel, String recipient, String subject, String content);

    NotificationResponse sendAppointmentCreated(Appointment appointment);

    NotificationResponse sendAppointmentCancelled(Appointment appointment, String reason);

    NotificationResponse sendAppointmentReminder(Appointment appointment);

    NotificationResponse sendPaymentReceived(Bill bill, Double amount);

    PageResponse<NotificationResponse> getAll(Pageable pageable);

    PageResponse<NotificationResponse> getByRecipient(String recipient, Pageable pageable);

    void markAsRead(Long id);
}
