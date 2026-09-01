package com.sunrise.dental.scheduled;

import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Sends automated appointment reminders to patients 24 hours before their visit.
 * Runs every morning at 07:00. (Advanced feature - reduces no-shows.)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${app.reminder.cron:0 0 7 * * *}")
    public void sendTomorrowReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> upcoming = appointmentRepository.findByAppointmentDate(tomorrow).stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED
                        || a.getStatus() == AppointmentStatus.CONFIRMED)
                .toList();
        log.info("Sending reminders for {} appointment(s) on {}", upcoming.size(), tomorrow);
        for (Appointment appointment : upcoming) {
            try {
                notificationService.sendAppointmentReminder(appointment);
            } catch (Exception ex) {
                log.warn("Reminder failed for appointment {}: {}", appointment.getAppointmentNumber(), ex.getMessage());
            }
        }
    }
}
