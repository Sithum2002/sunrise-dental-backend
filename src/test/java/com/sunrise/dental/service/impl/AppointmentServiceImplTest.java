package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
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
import com.sunrise.dental.enums.Gender;
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
import com.sunrise.dental.service.NumberSequenceService;
import com.sunrise.dental.specification.AppointmentSpecifications;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DentistRepository dentistRepository;
    @Mock
    private TreatmentRepository treatmentRepository;
    @Mock
    private AppointmentMapper appointmentMapper;
    @Mock
    private NumberSequenceService numberSequenceService;
    @Mock
    private AuditService auditService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Dentist dentist;
    private Treatment treatment;
    private Appointment appointment;
    private AppointmentResponse appointmentResponse;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .contactNumber("0771234567")
                .email("john.doe@example.com")
                .active(true)
                .build();

        dentist = Dentist.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .status(DentistStatus.AVAILABLE)
                .build();

        treatment = Treatment.builder()
                .id(3L)
                .name("Dental Cleaning")
                .code("TRT-CLEAN")
                .durationMinutes(30)
                .cost(5000.0)
                .active(true)
                .build();

        appointment = Appointment.builder()
                .id(1L)
                .appointmentNumber("AP-2026-0001")
                .patient(patient)
                .dentist(dentist)
                .treatment(treatment)
                .appointmentDate(futureWeekday())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .status(AppointmentStatus.SCHEDULED)
                .build();

        appointmentResponse = new AppointmentResponse(
                1L, "AP-2026-0001", 1L, "John Doe", "0771234567",
                2L, "Jane Smith", 3L, "Dental Cleaning", "TRT-CLEAN",
                futureWeekday(), LocalTime.of(9, 0), LocalTime.of(9, 30),
                AppointmentStatus.SCHEDULED, null, null, null);
    }

    private AppointmentRequest validRequest() {
        return AppointmentRequest.builder()
                .patientId(1L)
                .dentistId(2L)
                .treatmentId(3L)
                .appointmentDate(futureWeekday())
                .startTime(LocalTime.of(9, 0))
                .build();
    }

    @Nested
    @DisplayName("register() - Register new appointment")
    class Register {

        @Test
        @DisplayName("registers a valid appointment successfully")
        void register_success() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(dentist));
            when(treatmentRepository.findById(3L)).thenReturn(Optional.of(treatment));
            when(appointmentRepository.existsOverlappingSlot(2L, futureWeekday(), LocalTime.of(9, 0)))
                    .thenReturn(false);
            when(numberSequenceService.nextAppointmentNumber()).thenReturn("AP-2026-0001");
            when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
                ((Appointment) inv.getArgument(0)).setId(1L);
                return inv.getArgument(0);
            });
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

            AppointmentResponse result = appointmentService.register(validRequest());

            assertNotNull(result);
            assertEquals("AP-2026-0001", result.appointmentNumber());
            assertEquals(AppointmentStatus.SCHEDULED, result.status());

            verify(auditService).log(eq("CREATE"), eq("Appointment"), eq(1L), anyString());
            verify(eventPublisher).publishEvent(any(AppointmentCreatedEvent.class));
            verify(appointmentRepository).save(any(Appointment.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when patient does not exist")
        void register_patientNotFound() {
            when(patientRepository.findById(1L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> appointmentService.register(validRequest()));
            verify(appointmentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when dentist does not exist")
        void register_dentistNotFound() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> appointmentService.register(validRequest()));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when treatment does not exist")
        void register_treatmentNotFound() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(dentist));
            when(treatmentRepository.findById(3L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> appointmentService.register(validRequest()));
        }

        @Test
        @DisplayName("throws BusinessRuleException for past date")
        void register_pastDate() {
            AppointmentRequest request = validRequest();
            request.setAppointmentDate(LocalDate.now().minusDays(1));

            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(dentist));
            when(treatmentRepository.findById(3L)).thenReturn(Optional.of(treatment));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> appointmentService.register(request));
            assertTrue(ex.getMessage().contains("past date"));
        }

        @Test
        @DisplayName("throws BusinessRuleException for weekend")
        void register_weekend() {
            LocalDate saturday = getNextWeekend();
            AppointmentRequest request = validRequest();
            request.setAppointmentDate(saturday);

            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(dentist));
            when(treatmentRepository.findById(3L)).thenReturn(Optional.of(treatment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.register(request));
        }

        @Test
        @DisplayName("throws BusinessRuleException for out-of-hours time")
        void register_outOfHours() {
            AppointmentRequest request = validRequest();
            request.setStartTime(LocalTime.of(7, 0));

            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(dentist));
            when(treatmentRepository.findById(3L)).thenReturn(Optional.of(treatment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.register(request));
        }

        @Test
        @DisplayName("throws BusinessRuleException during lunch break")
        void register_duringLunch() {
            AppointmentRequest request = validRequest();
            request.setStartTime(LocalTime.of(13, 0));

            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(dentist));
            when(treatmentRepository.findById(3L)).thenReturn(Optional.of(treatment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.register(request));
        }

        @Test
        @DisplayName("throws BusinessRuleException when dentist is not available")
        void register_dentistUnavailable() {
            dentist.setStatus(DentistStatus.ON_LEAVE);
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(dentist));
            when(treatmentRepository.findById(3L)).thenReturn(Optional.of(treatment));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> appointmentService.register(validRequest()));
            assertTrue(ex.getMessage().contains("not available"));
        }

        @Test
        @DisplayName("throws BusinessRuleException when slot already booked")
        void register_slotTaken() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(dentist));
            when(treatmentRepository.findById(3L)).thenReturn(Optional.of(treatment));
            when(appointmentRepository.existsOverlappingSlot(2L, futureWeekday(), LocalTime.of(9, 0)))
                    .thenReturn(true);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> appointmentService.register(validRequest()));
            assertTrue(ex.getMessage().contains("already has an appointment"));
        }

        @Test
        @DisplayName("throws BusinessRuleException when end time exceeds closing hours")
        void register_endAfterClosing() {
            treatment.setDurationMinutes(600);
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(dentistRepository.findById(2L)).thenReturn(Optional.of(dentist));
            when(treatmentRepository.findById(3L)).thenReturn(Optional.of(treatment));
            when(appointmentRepository.existsOverlappingSlot(2L, futureWeekday(), LocalTime.of(9, 0)))
                    .thenReturn(false);

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> appointmentService.register(validRequest()));
            assertTrue(ex.getMessage().contains("closing time"));
        }
    }

    @Nested
    @DisplayName("getById() and getByAppointmentNumber()")
    class GetById {

        @Test
        @DisplayName("returns appointment by id")
        void getById_success() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);

            AppointmentResponse result = appointmentService.getById(1L);

            assertNotNull(result);
            assertEquals("AP-2026-0001", result.appointmentNumber());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when appointment does not exist")
        void getById_notFound() {
            when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> appointmentService.getById(99L));
        }

        @Test
        @DisplayName("returns appointment by appointment number")
        void getByAppointmentNumber_success() {
            when(appointmentRepository.findByAppointmentNumber("AP-2026-0001"))
                    .thenReturn(Optional.of(appointment));
            when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);

            AppointmentResponse result = appointmentService.getByAppointmentNumber("AP-2026-0001");

            assertNotNull(result);
            assertEquals("AP-2026-0001", result.appointmentNumber());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when appointment number not found")
        void getByAppointmentNumber_notFound() {
            when(appointmentRepository.findByAppointmentNumber("NOPE-123"))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> appointmentService.getByAppointmentNumber("NOPE-123"));
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("returns paginated appointments when no filters provided")
        void getAll_noFilters() {
            Page<Appointment> page = new PageImpl<>(List.of(appointment));
            Pageable pageable = PageRequest.of(0, 10);
            when(appointmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);

            PageResponse<AppointmentResponse> result =
                    appointmentService.getAll(null, null, null, null, null, pageable);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals(1, result.totalElements());
            assertEquals(0, result.page());
            verify(appointmentRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("returns empty page when no appointments match")
        void getAll_empty() {
            Page<Appointment> page = new PageImpl<>(Collections.emptyList());
            Pageable pageable = PageRequest.of(0, 10);
            when(appointmentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

            PageResponse<AppointmentResponse> result =
                    appointmentService.getAll(null, null, null, null, null, pageable);

            assertTrue(result.content().isEmpty());
            assertEquals(0, result.totalElements());
        }
    }

    @Nested
    @DisplayName("reschedule()")
    class Reschedule {

        private RescheduleRequest rescheduleRequest;

        @BeforeEach
        void setUpReschedule() {
            rescheduleRequest = RescheduleRequest.builder()
                    .appointmentDate(LocalDate.now().plusDays(5))
                    .startTime(LocalTime.of(14, 0))
                    .build();
        }

        @Test
        @DisplayName("reschedules a scheduled appointment")
        void reschedule_success() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.existsOverlappingRange(2L, LocalDate.now().plusDays(5),
                    LocalTime.of(14, 0), LocalTime.of(14, 30), 1L)).thenReturn(false);
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

            AppointmentResponse result = appointmentService.reschedule(1L, rescheduleRequest);

            assertNotNull(result);
            assertEquals(AppointmentStatus.SCHEDULED, appointment.getStatus());
            assertEquals(LocalDate.now().plusDays(5), appointment.getAppointmentDate());
            assertEquals(LocalTime.of(14, 0), appointment.getStartTime());
            verify(auditService).log(eq("RESCHEDULE"), eq("Appointment"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws BusinessRuleException for completed appointment")
        void reschedule_completed() {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                    () -> appointmentService.reschedule(1L, rescheduleRequest));
            assertTrue(ex.getMessage().contains("Only scheduled or confirmed"));
        }

        @Test
        @DisplayName("throws BusinessRuleException for cancelled appointment")
        void reschedule_cancelled() {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertThrows(BusinessRuleException.class,
                    () -> appointmentService.reschedule(1L, rescheduleRequest));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException if appointment not found")
        void reschedule_notFound() {
            when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> appointmentService.reschedule(99L, rescheduleRequest));
        }
    }

    @Nested
    @DisplayName("cancel()")
    class Cancel {

        @Test
        @DisplayName("cancels a scheduled appointment")
        void cancel_success() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

            String reason = "Patient sick";
            AppointmentResponse result = appointmentService.cancel(1L, reason);

            assertNotNull(result);
            assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
            verify(auditService).log(eq("CANCEL"), eq("Appointment"), eq(1L), anyString());
            verify(eventPublisher).publishEvent(any(AppointmentCancelledEvent.class));
        }

        @Test
        @DisplayName("calls cancel with null reason")
        void cancel_nullReason() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

            appointmentService.cancel(1L, null);

            verify(eventPublisher).publishEvent(any(AppointmentCancelledEvent.class));
        }

        @Test
        @DisplayName("throws BusinessRuleException for completed appointment")
        void cancel_completed() {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.cancel(1L, "test"));
        }

        @Test
        @DisplayName("throws BusinessRuleException for already-cancelled appointment")
        void cancel_alreadyCancelled() {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.cancel(1L, "test"));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException if not found")
        void cancel_notFound() {
            when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> appointmentService.cancel(99L, "test"));
        }
    }

    @Nested
    @DisplayName("confirm()")
    class Confirm {

        @Test
        @DisplayName("confirms a scheduled appointment")
        void confirm_success() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

            AppointmentResponse result = appointmentService.confirm(1L);

            assertEquals(AppointmentStatus.CONFIRMED, appointment.getStatus());
            verify(auditService).log(eq("CONFIRM"), eq("Appointment"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws BusinessRuleException when appointment is not scheduled")
        void confirm_notScheduled() {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.confirm(1L));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException if not found")
        void confirm_notFound() {
            when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> appointmentService.confirm(99L));
        }
    }

    @Nested
    @DisplayName("complete()")
    class Complete {

        @Test
        @DisplayName("completes a scheduled appointment with notes")
        void complete_success() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

            AppointmentResponse result = appointmentService.complete(1L, "Treatment went well");

            assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
            assertEquals("Treatment went well", appointment.getCompletedNotes());
            verify(auditService).log(eq("COMPLETE"), eq("Appointment"), eq(1L), anyString());
            verify(eventPublisher).publishEvent(any(AppointmentCompletedEvent.class));
        }

        @Test
        @DisplayName("completes with null notes")
        void complete_nullNotes() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

            appointmentService.complete(1L, null);

            assertNull(appointment.getCompletedNotes());
        }

        @Test
        @DisplayName("throws BusinessRuleException for cancelled appointment")
        void complete_cancelled() {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.complete(1L, "notes"));
        }

        @Test
        @DisplayName("throws BusinessRuleException for no-show appointment")
        void complete_noShow() {
            appointment.setStatus(AppointmentStatus.NO_SHOW);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.complete(1L, "notes"));
        }
    }

    @Nested
    @DisplayName("markNoShow()")
    class MarkNoShow {

        @Test
        @DisplayName("marks scheduled appointment as no-show")
        void markNoShow_scheduled() {
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

            AppointmentResponse result = appointmentService.markNoShow(1L);

            assertEquals(AppointmentStatus.NO_SHOW, appointment.getStatus());
            verify(auditService).log(eq("NO_SHOW"), eq("Appointment"), eq(1L), anyString());
        }

        @Test
        @DisplayName("marks confirmed appointment as no-show")
        void markNoShow_confirmed() {
            appointment.setStatus(AppointmentStatus.CONFIRMED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
            when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);
            when(appointmentMapper.toResponse(any(Appointment.class))).thenReturn(appointmentResponse);

            appointmentService.markNoShow(1L);
            assertEquals(AppointmentStatus.NO_SHOW, appointment.getStatus());
        }

        @Test
        @DisplayName("throws BusinessRuleException for completed appointment")
        void markNoShow_completed() {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.markNoShow(1L));
        }

        @Test
        @DisplayName("throws BusinessRuleException for no-show already")
        void markNoShow_alreadyNoShow() {
            appointment.setStatus(AppointmentStatus.NO_SHOW);
            when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

            assertThrows(BusinessRuleException.class, () -> appointmentService.markNoShow(1L));
        }
    }

    @Nested
    @DisplayName("Query methods")
    class QueryMethods {

        @Test
        @DisplayName("getTodayAppointments returns sorted list")
        void getTodayAppointments() {
            LocalDate today = LocalDate.now();
            Appointment late = Appointment.builder()
                    .id(2L).appointmentNumber("AP-2026-0002")
                    .patient(patient).dentist(dentist).treatment(treatment)
                    .appointmentDate(today)
                    .startTime(LocalTime.of(11, 0))
                    .endTime(LocalTime.of(11, 30))
                    .status(AppointmentStatus.SCHEDULED)
                    .build();
            Appointment early = Appointment.builder()
                    .id(3L).appointmentNumber("AP-2026-0003")
                    .patient(patient).dentist(dentist).treatment(treatment)
                    .appointmentDate(today)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(8, 30))
                    .status(AppointmentStatus.SCHEDULED)
                    .build();

            AppointmentResponse earlyResponse = new AppointmentResponse(
                    3L, "AP-2026-0003", 1L, "John Doe", "0771234567",
                    2L, "Jane Smith", 3L, "Dental Cleaning", "TRT-CLEAN",
                    today, LocalTime.of(8, 0), LocalTime.of(8, 30),
                    AppointmentStatus.SCHEDULED, null, null, null);

            when(appointmentRepository.findByAppointmentDate(today))
                    .thenReturn(List.of(late, early));
            when(appointmentMapper.toResponse(early)).thenReturn(earlyResponse);

            List<AppointmentResponse> result = appointmentService.getTodayAppointments();

            assertNotNull(result);
            verify(appointmentRepository).findByAppointmentDate(today);
        }

        @Test
        @DisplayName("getUpcomingToday returns upcoming appointments")
        void getUpcomingToday() {
            LocalDate today = LocalDate.now();
            when(appointmentRepository.findUpcomingToday(eq(today), any(LocalTime.class)))
                    .thenReturn(List.of(appointment));
            when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);

            List<AppointmentResponse> result = appointmentService.getUpcomingToday();

            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("getByPatientId returns patient appointments")
        void getByPatientId() {
            Page<Appointment> page = new PageImpl<>(List.of(appointment));
            when(appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(1L, Pageable.unpaged()))
                    .thenReturn(page);
            when(appointmentMapper.toResponse(appointment)).thenReturn(appointmentResponse);

            List<AppointmentResponse> result = appointmentService.getByPatientId(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(appointmentRepository).findByPatientIdOrderByAppointmentDateDesc(1L, Pageable.unpaged());
        }
    }

    private LocalDate futureWeekday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek().getValue() >= 6) {
            date = date.plusDays(1);
        }
        return date;
    }

    private LocalDate getNextWeekend() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek().getValue() < 6) {
            date = date.plusDays(1);
        }
        return date;
    }
}
