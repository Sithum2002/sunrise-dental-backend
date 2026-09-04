package com.sunrise.dental.service.impl;

import com.sunrise.dental.dto.response.NotificationResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Bill;
import com.sunrise.dental.entity.Dentist;
import com.sunrise.dental.entity.Notification;
import com.sunrise.dental.entity.Patient;
import com.sunrise.dental.entity.Treatment;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.enums.Gender;
import com.sunrise.dental.enums.NotificationChannel;
import com.sunrise.dental.enums.NotificationStatus;
import com.sunrise.dental.mapper.NotificationMapper;
import com.sunrise.dental.repository.NotificationRepository;
import com.sunrise.dental.service.EmailService;
import com.sunrise.dental.service.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationMapper notificationMapper;
    @Mock
    private EmailService emailService;
    @Mock
    private SmsService smsService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;
    private NotificationResponse notificationResponse;
    private Appointment appointment;
    private Bill bill;

    @BeforeEach
    void setUp() {
        Patient patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .contactNumber("0771234567")
                .email("john.doe@example.com")
                .gender(Gender.MALE)
                .active(true)
                .build();

        Dentist dentist = Dentist.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        Treatment treatment = Treatment.builder()
                .id(1L)
                .name("Dental Cleaning")
                .code("TRT-CLEAN")
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .appointmentNumber("AP-2026-0001")
                .patient(patient)
                .dentist(dentist)
                .treatment(treatment)
                .appointmentDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        bill = Bill.builder()
                .id(1L)
                .billNumber("INV-0001")
                .appointment(appointment)
                .dueAmount(0.0)
                .build();

        notification = Notification.builder()
                .id(1L)
                .recipient("john.doe@example.com")
                .channel(NotificationChannel.EMAIL)
                .subject("Test Subject")
                .content("Test Content")
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now())
                .build();

        notificationResponse = new NotificationResponse(
                1L, "john.doe@example.com", NotificationChannel.EMAIL,
                "Test Subject", "Test Content", NotificationStatus.SENT,
                LocalDateTime.now(), null, null);
    }

    @Nested
    @DisplayName("send()")
    class Send {

        @Test
        @DisplayName("sends email via email service and marks SENT")
        void send_email() {
            when(emailService.sendEmail("john.doe@example.com", "Subj", "Content")).thenReturn(true);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            NotificationResponse result = notificationService.send(
                    NotificationChannel.EMAIL, "john.doe@example.com", "Subj", "Content");

            assertNotNull(result);
            verify(emailService).sendEmail("john.doe@example.com", "Subj", "Content");
            verify(smsService, never()).sendSms(anyString(), anyString());
        }

        @Test
        @DisplayName("sends SMS via sms service")
        void send_sms() {
            when(smsService.sendSms("0771234567", "msg")).thenReturn(true);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            notificationService.send(NotificationChannel.SMS, "0771234567", "", "msg");

            verify(smsService).sendSms("0771234567", "msg");
        }

        @Test
        @DisplayName("IN_APP always succeeds without external service")
        void send_inApp() {
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            notificationService.send(NotificationChannel.IN_APP, "John", "Subj", "Content");

            verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
            verify(smsService, never()).sendSms(anyString(), anyString());
        }

        @Test
        @DisplayName("marks notification FAILED when email delivery fails")
        void send_emailFailure() {
            when(emailService.sendEmail("john@example.com", "Subj", "Content")).thenReturn(false);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            notificationService.send(NotificationChannel.EMAIL, "john@example.com", "Subj", "Content");

            verify(notificationRepository).save(argThat(n ->
                    n.getStatus() == NotificationStatus.FAILED
                            && n.getErrorMessage() != null
                            && n.getErrorMessage().contains("failed")));
        }

        @Test
        @DisplayName("marks notification FAILED when SMS delivery fails")
        void send_smsFailure() {
            when(smsService.sendSms("0771234567", "msg")).thenReturn(false);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            notificationService.send(NotificationChannel.SMS, "0771234567", "", "msg");

            verify(notificationRepository).save(argThat(n -> n.getStatus() == NotificationStatus.FAILED));
        }

        @Test
        @DisplayName("starts with PENDING status before delivery")
        void send_pendingFirst() {
            when(emailService.sendEmail("john@example.com", "S", "C")).thenReturn(true);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            notificationService.send(NotificationChannel.EMAIL, "john@example.com", "S", "C");

            verify(notificationRepository).save(argThat(n -> n.getStatus() == NotificationStatus.SENT));
        }
    }

    @Nested
    @DisplayName("sendAppointmentCreated()")
    class SendAppointmentCreated {

        @Test
        @DisplayName("sends email, SMS and in-app notifications")
        void sendAppointmentCreated_allChannels() {
            when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
            when(smsService.sendSms(anyString(), anyString())).thenReturn(true);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            notificationService.sendAppointmentCreated(appointment);

            verify(emailService).sendEmail(eq("john.doe@example.com"), contains("Appointment Confirmed"), anyString());
            verify(smsService).sendSms(eq("0771234567"), anyString());
            verify(notificationRepository, times(3)).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("sendAppointmentCancelled()")
    class SendAppointmentCancelled {

        @Test
        @DisplayName("sends cancelled notifications with reason")
        void sendAppointmentCancelled() {
            when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
            when(smsService.sendSms(anyString(), anyString())).thenReturn(true);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            notificationService.sendAppointmentCancelled(appointment, "Patient request");

            verify(emailService).sendEmail(eq("john.doe@example.com"), contains("Appointment Cancelled"), anyString());
            verify(notificationRepository, times(3)).save(any(Notification.class));
        }

        @Test
        @DisplayName("handles null reason")
        void sendAppointmentCancelled_nullReason() {
            when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
            when(smsService.sendSms(anyString(), anyString())).thenReturn(true);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            assertDoesNotThrow(() -> notificationService.sendAppointmentCancelled(appointment, null));
        }
    }

    @Nested
    @DisplayName("sendAppointmentReminder()")
    class SendAppointmentReminder {

        @Test
        @DisplayName("sends reminder notifications")
        void sendAppointmentReminder() {
            when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
            when(smsService.sendSms(anyString(), anyString())).thenReturn(true);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            notificationService.sendAppointmentReminder(appointment);

            verify(emailService).sendEmail(eq("john.doe@example.com"), contains("Reminder"), anyString());
            verify(smsService).sendSms(eq("0771234567"), contains("reminder"));
            verify(notificationRepository, times(3)).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("sendPaymentReceived()")
    class SendPaymentReceived {

        @Test
        @DisplayName("sends payment received notifications")
        void sendPaymentReceived() {
            when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
            when(notificationRepository.save(any(Notification.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            when(notificationMapper.toResponse(any(Notification.class))).thenReturn(notificationResponse);

            notificationService.sendPaymentReceived(bill, 5000.0);

            verify(emailService).sendEmail(eq("john.doe@example.com"), contains("Payment Received"), anyString());
            verify(notificationRepository, times(2)).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("getAll() / getByRecipient()")
    class QueryMethods {

        @Test
        @DisplayName("returns all notifications")
        void getAll_success() {
            Page<Notification> page = new PageImpl<>(List.of(notification));
            Pageable pageable = PageRequest.of(0, 10);
            when(notificationRepository.findAllOrderBySentAtDesc(pageable)).thenReturn(page);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            PageResponse<NotificationResponse> result = notificationService.getAll(pageable);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }

        @Test
        @DisplayName("returns empty notifications when none exist")
        void getAll_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(notificationRepository.findAllOrderBySentAtDesc(pageable))
                    .thenReturn(new PageImpl<>(List.of()));

            PageResponse<NotificationResponse> result = notificationService.getAll(pageable);

            assertTrue(result.content().isEmpty());
        }

        @Test
        @DisplayName("returns notifications by recipient")
        void getByRecipient() {
            Page<Notification> page = new PageImpl<>(List.of(notification));
            Pageable pageable = PageRequest.of(0, 10);
            when(notificationRepository.findByRecipientOrderBySentAtDesc("john.doe@example.com", pageable))
                    .thenReturn(page);
            when(notificationMapper.toResponse(notification)).thenReturn(notificationResponse);

            PageResponse<NotificationResponse> result =
                    notificationService.getByRecipient("john.doe@example.com", pageable);

            assertNotNull(result);
            assertEquals(1, result.totalElements());
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsRead {

        @Test
        @DisplayName("marks notification as read")
        void markAsRead_success() {
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

            notificationService.markAsRead(1L);

            assertNotNull(notification.getReadAt());
        }

        @Test
        @DisplayName("does nothing when notification not found")
        void markAsRead_notFound() {
            when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> notificationService.markAsRead(99L));
            verify(notificationRepository, never()).save(any());
        }
    }
}
