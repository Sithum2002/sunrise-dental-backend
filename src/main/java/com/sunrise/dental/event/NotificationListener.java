package com.sunrise.dental.event;

import com.sunrise.dental.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Observer pattern - reacts to domain events and dispatches notifications.
 * Listens after the transaction commits so notifications only fire for
 * successfully persisted changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentCreated(AppointmentCreatedEvent event) {
        try {
            notificationService.sendAppointmentCreated(event.appointment());
        } catch (Exception ex) {
            log.warn("Notification dispatch failed for appointment creation: {}", ex.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentCancelled(AppointmentCancelledEvent event) {
        try {
            notificationService.sendAppointmentCancelled(event.appointment(), event.reason());
        } catch (Exception ex) {
            log.warn("Notification dispatch failed for appointment cancellation: {}", ex.getMessage());
        }
    }

    @EventListener
    public void onAppointmentCompleted(AppointmentCompletedEvent event) {
        log.info("Appointment {} completed - billing can now be created.", event.appointment().getAppointmentNumber());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentReceived(PaymentReceivedEvent event) {
        try {
            notificationService.sendPaymentReceived(event.bill(), event.amount());
        } catch (Exception ex) {
            log.warn("Payment receipt notification failed: {}", ex.getMessage());
        }
    }
}
