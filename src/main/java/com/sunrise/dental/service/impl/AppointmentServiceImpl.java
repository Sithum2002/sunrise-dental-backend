package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.constant.AppConstants;
import com.sunrise.dental.dto.request.AppointmentRequest;
import com.sunrise.dental.dto.request.RescheduleRequest;
import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Dentist;
import com.sunrise.dental.entity.Patient;
import com.sunrise.dental.entity.Treatment;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.enums.DentistStatus;
import com.sunrise.dental.event.AppointmentCancelledEvent;
import com.sunrise.dental.event.AppointmentCompletedEvent;
import com.sunrise.dental.event.AppointmentCreatedEvent;
import com.sunrise.dental.exception.BusinessRuleException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.AppointmentMapper;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.DentistRepository;
import com.sunrise.dental.repository.PatientRepository;
import com.sunrise.dental.repository.TreatmentRepository;
import com.sunrise.dental.service.AppointmentService;
import com.sunrise.dental.service.NumberSequenceService;
import com.sunrise.dental.specification.AppointmentSpecifications;
import com.sunrise.dental.util.AppDateUtils;
import com.sunrise.dental.validation.AppointmentTimeValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Appointment scheduling service - implements the core business rules:
 * <ul>
 *     <li>appointments must be within clinic hours and not on weekends</li>
 *     <li>a dentist cannot be double-booked for the same slot</li>
 *     <li>unique appointment numbers are generated automatically</li>
 *     <li>status transitions are strictly controlled</li>
 *     <li>domain events notify patients via email/SMS (Observer)</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DentistRepository dentistRepository;
    private final TreatmentRepository treatmentRepository;
    private final AppointmentMapper appointmentMapper;
    private final NumberSequenceService numberSequenceService;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AppointmentResponse register(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + request.getPatientId()));
        Dentist dentist = dentistRepository.findById(request.getDentistId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found with id " + request.getDentistId()));
        Treatment treatment = treatmentRepository.findById(request.getTreatmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Treatment not found with id " + request.getTreatmentId()));

        validateSlot(dentist, request.getAppointmentDate(), request.getStartTime(), null);

        LocalTime endTime = request.getStartTime().plusMinutes(treatment.getDurationMinutes());
        if (endTime.isAfter(LocalTime.of(AppConstants.CLOSING_HOUR, 0))) {
            throw new BusinessRuleException("The treatment cannot finish after clinic closing time (18:00).");
        }

        Appointment appointment = Appointment.builder()
                .appointmentNumber(numberSequenceService.nextAppointmentNumber())
                .patient(patient)
                .dentist(dentist)
                .treatment(treatment)
                .appointmentDate(request.getAppointmentDate())
                .startTime(request.getStartTime())
                .endTime(endTime)
                .status(AppointmentStatus.SCHEDULED)
                .notes(request.getNotes())
                .build();
        appointmentRepository.save(appointment);

        auditService.log("CREATE", "Appointment", appointment.getId(),
                "Registered appointment " + appointment.getAppointmentNumber() + " for patient "
                        + patient.getFullName() + " with Dr. " + dentist.getLastName());
        eventPublisher.publishEvent(new AppointmentCreatedEvent(appointment));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        return appointmentMapper.toResponse(findAppointment(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getByAppointmentNumber(String appointmentNumber) {
        Appointment appointment = appointmentRepository.findByAppointmentNumber(appointmentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentNumber));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getAll(LocalDate from, LocalDate to, Long dentistId,
                                                    Long patientId, String status, Pageable pageable) {
        Page<Appointment> page = appointmentRepository.findAll(
                AppointmentSpecifications.withFilters(from, to, dentistId, patientId, status), pageable);
        return new PageResponse<>(
                page.getContent().stream().map(appointmentMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional
    public AppointmentResponse reschedule(Long id, RescheduleRequest request) {
        Appointment appointment = findAppointment(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleException("Only scheduled or confirmed appointments can be rescheduled.");
        }
        validateSlot(appointment.getDentist(), request.getAppointmentDate(), request.getStartTime(), appointment.getId());

        long duration = java.time.Duration.between(appointment.getStartTime(), appointment.getEndTime()).toMinutes();
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStartTime(request.getStartTime());
        appointment.setEndTime(request.getStartTime().plusMinutes(duration));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);

        auditService.log("RESCHEDULE", "Appointment", id,
                "Rescheduled appointment " + appointment.getAppointmentNumber() + " to "
                        + request.getAppointmentDate() + " " + request.getStartTime());
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancel(Long id, String reason) {
        Appointment appointment = findAppointment(id);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BusinessRuleException("This appointment cannot be cancelled in its current state.");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        auditService.log("CANCEL", "Appointment", id,
                "Cancelled appointment " + appointment.getAppointmentNumber() + " (" + reason + ")");
        eventPublisher.publishEvent(new AppointmentCancelledEvent(appointment, reason));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse confirm(Long id) {
        Appointment appointment = findAppointment(id);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessRuleException("Only scheduled appointments can be confirmed.");
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
        auditService.log("CONFIRM", "Appointment", id, "Confirmed appointment " + appointment.getAppointmentNumber());
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse complete(Long id, String notes) {
        Appointment appointment = findAppointment(id);
        if (appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessRuleException("A cancelled or no-show appointment cannot be completed.");
        }
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setCompletedNotes(notes);
        appointmentRepository.save(appointment);

        auditService.log("COMPLETE", "Appointment", id, "Completed appointment " + appointment.getAppointmentNumber());
        eventPublisher.publishEvent(new AppointmentCompletedEvent(appointment));
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse markNoShow(Long id) {
        Appointment appointment = findAppointment(id);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED
                && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BusinessRuleException("Only scheduled or confirmed appointments can be marked as no-show.");
        }
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepository.save(appointment);
        auditService.log("NO_SHOW", "Appointment", id, "Marked no-show " + appointment.getAppointmentNumber());
        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getTodayAppointments() {
        return appointmentRepository.findByAppointmentDate(LocalDate.now()).stream()
                .sorted(java.util.Comparator.comparing(Appointment::getStartTime))
                .map(appointmentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingToday() {
        return appointmentRepository.findUpcomingToday(LocalDate.now(), LocalTime.now()).stream()
                .map(appointmentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByPatientId(Long patientId) {
        return appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId, Pageable.unpaged())
                .getContent().stream().map(appointmentMapper::toResponse).toList();
    }

    private void validateSlot(Dentist dentist, LocalDate date, LocalTime startTime, Long excludeId) {
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Appointments cannot be booked for a past date.");
        }
        if (AppDateUtils.isWeekend(date)) {
            throw new BusinessRuleException("The clinic is closed on weekends. Please select a weekday.");
        }
        if (startTime == null) {
            throw new BusinessRuleException("Start time is required.");
        }
        if (!new AppointmentTimeValidator().isValid(startTime, null)) {
            throw new BusinessRuleException("Appointment time must be between 08:00 and 17:30 and outside the lunch break (12:30-13:30).");
        }
        if (dentist.getStatus() != DentistStatus.AVAILABLE) {
            throw new BusinessRuleException("Dr. " + dentist.getLastName() + " is currently not available for new bookings.");
        }
        if (excludeId == null) {
            if (appointmentRepository.existsOverlappingSlot(dentist.getId(), date, startTime)) {
                throw new BusinessRuleException("Dr. " + dentist.getLastName()
                        + " already has an appointment at that time. Please choose another slot.");
            }
        } else {
            long duration = 30;
            LocalTime end = startTime.plusMinutes(duration);
            if (appointmentRepository.existsOverlappingRange(dentist.getId(), date, startTime, end, excludeId)) {
                throw new BusinessRuleException("Dr. " + dentist.getLastName()
                        + " already has an appointment at that time. Please choose another slot.");
            }
        }
    }

    private Appointment findAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));
    }
}
