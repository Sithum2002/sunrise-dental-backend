package com.sunrise.dental.service.impl;

import com.sunrise.dental.constant.AppConstants;
import com.sunrise.dental.dto.response.NotificationResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Bill;
import com.sunrise.dental.entity.Notification;
import com.sunrise.dental.enums.NotificationChannel;
import com.sunrise.dental.enums.NotificationStatus;
import com.sunrise.dental.mapper.NotificationMapper;
import com.sunrise.dental.repository.NotificationRepository;
import com.sunrise.dental.service.EmailService;
import com.sunrise.dental.service.NotificationService;
import com.sunrise.dental.service.SmsService;
import com.sunrise.dental.util.AppDateUtils;
import com.sunrise.dental.util.NumberUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Notification implementation - dispatches through the email/SMS strategies,
 * persists a record and returns the response DTO.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;
    private final SmsService smsService;

    @Override
    @Transactional
    public NotificationResponse send(NotificationChannel channel, String recipient,
                                     String subject, String content) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .channel(channel)
                .subject(subject)
                .content(content)
                .status(NotificationStatus.PENDING)
                .build();

        boolean delivered = switch (channel) {
            case EMAIL -> emailService.sendEmail(recipient, subject, content);
            case SMS -> smsService.sendSms(recipient, content);
            case IN_APP -> true;
        };

        notification.setStatus(delivered ? NotificationStatus.SENT : NotificationStatus.FAILED);
        notification.setSentAt(LocalDateTime.now());
        if (!delivered) {
            notification.setErrorMessage("Delivery through channel " + channel + " failed.");
        }
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public NotificationResponse sendAppointmentCreated(Appointment appointment) {
        String when = AppDateUtils.formatDate(appointment.getAppointmentDate()) + " at "
                + AppDateUtils.formatTime(appointment.getStartTime());
        String subject = "Appointment Confirmed - " + appointment.getAppointmentNumber();
        String body = "<html><body><h3>" + AppConstants.CLINIC_NAME + "</h3>"
                + "<p>Dear <b>" + appointment.getPatient().getFullName() + "</b>,</p>"
                + "<p>Your appointment has been <b>confirmed</b>.</p>"
                + "<p>Appointment No: <b>" + appointment.getAppointmentNumber() + "</b><br>"
                + "Date: <b>" + when + "</b><br>"
                + "Dentist: <b>" + appointment.getDentist().getFirstName() + " " + appointment.getDentist().getLastName() + "</b><br>"
                + "Treatment: <b>" + appointment.getTreatment().getName() + "</b></p>"
                + "<p>Please arrive 15 minutes early.</p></body></html>";

        send(NotificationChannel.EMAIL, appointment.getPatient().getEmail(), subject, body);
        send(NotificationChannel.SMS, appointment.getPatient().getContactNumber(), "",
                "Sunrise Dental: Appointment " + appointment.getAppointmentNumber()
                        + " confirmed for " + when + ". Thank you!");
        return send(NotificationChannel.IN_APP, appointment.getPatient().getFullName(),
                subject, "Appointment " + appointment.getAppointmentNumber() + " confirmed for " + when + ".");
    }

    @Override
    @Transactional
    public NotificationResponse sendAppointmentCancelled(Appointment appointment, String reason) {
        String when = AppDateUtils.formatDate(appointment.getAppointmentDate()) + " at "
                + AppDateUtils.formatTime(appointment.getStartTime());
        String subject = "Appointment Cancelled - " + appointment.getAppointmentNumber();
        String body = "<html><body><h3>" + AppConstants.CLINIC_NAME + "</h3>"
                + "<p>Dear <b>" + appointment.getPatient().getFullName() + "</b>,</p>"
                + "<p>Your appointment <b>" + appointment.getAppointmentNumber() + "</b> scheduled for "
                + "<b>" + when + "</b> has been cancelled.</p>"
                + "<p>Reason: " + (reason == null ? "Not specified" : reason) + "</p>"
                + "<p>Please contact the clinic to reschedule.</p></body></html>";

        send(NotificationChannel.EMAIL, appointment.getPatient().getEmail(), subject, body);
        send(NotificationChannel.SMS, appointment.getPatient().getContactNumber(), "",
                "Sunrise Dental: Appointment " + appointment.getAppointmentNumber() + " for " + when + " was cancelled.");
        return send(NotificationChannel.IN_APP, appointment.getPatient().getFullName(),
                subject, "Appointment " + appointment.getAppointmentNumber() + " for " + when + " cancelled.");
    }

    @Override
    @Transactional
    public NotificationResponse sendAppointmentReminder(Appointment appointment) {
        String when = AppDateUtils.formatDate(appointment.getAppointmentDate()) + " at "
                + AppDateUtils.formatTime(appointment.getStartTime());
        String subject = "Reminder - Upcoming Appointment Tomorrow";
        String body = "<html><body><h3>" + AppConstants.CLINIC_NAME + "</h3>"
                + "<p>Dear <b>" + appointment.getPatient().getFullName() + "</b>,</p>"
                + "<p>This is a friendly reminder that you have an appointment tomorrow.</p>"
                + "<p>Appointment No: <b>" + appointment.getAppointmentNumber() + "</b><br>"
                + "Date: <b>" + when + "</b><br>"
                + "Dentist: <b>" + appointment.getDentist().getFirstName() + " " + appointment.getDentist().getLastName() + "</b></p>"
                + "<p>Call " + AppConstants.CLINIC_PHONE + " to reschedule if needed.</p></body></html>";

        send(NotificationChannel.EMAIL, appointment.getPatient().getEmail(), subject, body);
        send(NotificationChannel.SMS, appointment.getPatient().getContactNumber(), "",
                "Sunrise Dental reminder: Appointment " + appointment.getAppointmentNumber() + " tomorrow at " + when + ".");
        return send(NotificationChannel.IN_APP, appointment.getPatient().getFullName(),
                subject, "Reminder for appointment " + appointment.getAppointmentNumber() + " tomorrow.");
    }

    @Override
    @Transactional
    public NotificationResponse sendPaymentReceived(Bill bill, Double amount) {
        String subject = "Payment Received - " + bill.getBillNumber();
        String body = "<html><body><h3>" + AppConstants.CLINIC_NAME + "</h3>"
                + "<p>Dear <b>" + bill.getAppointment().getPatient().getFullName() + "</b>,</p>"
                + "<p>We have received a payment of <b>LKR " + NumberUtils.formatCurrency(amount)
                + "</b> against bill <b>" + bill.getBillNumber() + "</b>.</p>"
                + "<p>Outstanding balance: <b>LKR " + NumberUtils.formatCurrency(bill.getDueAmount()) + "</b></p>"
                + "<p>Thank you for choosing " + AppConstants.CLINIC_NAME + ".</p></body></html>";

        send(NotificationChannel.EMAIL, bill.getAppointment().getPatient().getEmail(), subject, body);
        return send(NotificationChannel.IN_APP, bill.getAppointment().getPatient().getFullName(),
                subject, "Payment of LKR " + NumberUtils.formatCurrency(amount) + " received for " + bill.getBillNumber() + ".");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getAll(Pageable pageable) {
        var page = notificationRepository.findAllOrderBySentAtDesc(pageable);
        return toResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getByRecipient(String recipient, Pageable pageable) {
        var page = notificationRepository.findByRecipientOrderBySentAtDesc(recipient, pageable);
        return toResponse(page);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> n.setReadAt(LocalDateTime.now()));
    }

    private PageResponse<NotificationResponse> toResponse(org.springframework.data.domain.Page<Notification> page) {
        return new PageResponse<>(
                page.getContent().stream().map(notificationMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
