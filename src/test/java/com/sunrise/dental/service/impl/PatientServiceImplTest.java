package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.request.PatientRequest;
import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.PatientResponse;
import com.sunrise.dental.entity.Appointment;
import com.sunrise.dental.entity.Dentist;
import com.sunrise.dental.entity.Patient;
import com.sunrise.dental.entity.Treatment;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.enums.BloodGroup;
import com.sunrise.dental.enums.Gender;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.AppointmentMapper;
import com.sunrise.dental.mapper.PatientMapper;
import com.sunrise.dental.repository.AppointmentRepository;
import com.sunrise.dental.repository.PatientRepository;
import com.sunrise.dental.service.NumberSequenceService;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientMapper patientMapper;
    @Mock
    private AppointmentMapper appointmentMapper;
    @Mock
    private NumberSequenceService numberSequenceService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient patient;
    private PatientResponse patientResponse;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .regNo("SD-P0001")
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .contactNumber("0771234567")
                .email("john.doe@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .bloodGroup(BloodGroup.O_POSITIVE)
                .active(true)
                .build();

        patientResponse = new PatientResponse(
                1L, "SD-P0001", "John", "Doe", "John Doe",
                "123 Main St", "0771234567", "john.doe@example.com",
                LocalDate.of(1990, 1, 1), Gender.MALE, BloodGroup.O_POSITIVE,
                null, null, null, true, null);
    }

    private PatientRequest validRequest() {
        return PatientRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .contactNumber("0771234567")
                .email("john.doe@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("returns paginated patients with no filters")
        void getAll_noFilters() {
            Page<Patient> page = new PageImpl<>(List.of(patient));
            Pageable pageable = PageRequest.of(0, 10);
            when(patientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            PageResponse<PatientResponse> result = patientService.getAll(pageable, null, null, null);

            assertNotNull(result);
            assertEquals(1, result.content().size());
            assertEquals("John Doe", result.content().get(0).fullName());
        }

        @Test
        @DisplayName("returns empty page when no patients")
        void getAll_empty() {
            Page<Patient> empty = new PageImpl<>(List.of());
            Pageable pageable = PageRequest.of(0, 10);
            when(patientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(empty);

            PageResponse<PatientResponse> result = patientService.getAll(pageable, null, null, null);

            assertTrue(result.content().isEmpty());
        }

        @Test
        @DisplayName("passes gender and bloodGroup filters")
        void getAll_withFilters() {
            Page<Patient> page = new PageImpl<>(List.of(patient));
            Pageable pageable = PageRequest.of(0, 10);
            when(patientRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            patientService.getAll(pageable, "John", "MALE", "O_POSITIVE");

            verify(patientRepository).findAll(any(Specification.class), eq(pageable));
        }
    }

    @Nested
    @DisplayName("getById() / getByRegNo()")
    class Get {

        @Test
        @DisplayName("returns patient by id")
        void getById_success() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            PatientResponse result = patientService.getById(1L);

            assertEquals("SD-P0001", result.regNo());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when patient not found")
        void getById_notFound() {
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> patientService.getById(99L));
        }

        @Test
        @DisplayName("returns patient by reg no")
        void getByRegNo_success() {
            when(patientRepository.findByRegNo("SD-P0001")).thenReturn(Optional.of(patient));
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            PatientResponse result = patientService.getByRegNo("SD-P0001");

            assertEquals("John Doe", result.fullName());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when reg no not found")
        void getByRegNo_notFound() {
            when(patientRepository.findByRegNo("NOPE")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> patientService.getByRegNo("NOPE"));
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("creates patient successfully")
        void create_success() {
            when(patientRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
            when(patientMapper.toEntity(any(PatientRequest.class))).thenReturn(patient);
            when(numberSequenceService.nextPatientRegNo()).thenReturn("SD-P0001");
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            PatientResponse result = patientService.create(validRequest());

            assertNotNull(result);
            assertEquals("SD-P0001", result.regNo());
            assertTrue(patient.isActive());
            verify(auditService).log(eq("CREATE"), eq("Patient"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email exists")
        void create_duplicateEmail() {
            when(patientRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> patientService.create(validRequest()));
            verify(patientRepository, never()).save(any());
        }

        @Test
        @DisplayName("flags minor patient registration in audit")
        void create_minorPatient() {
            PatientRequest request = validRequest();
            request.setDateOfBirth(LocalDate.now().minusYears(10));
            when(patientRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
            when(patientMapper.toEntity(any(PatientRequest.class))).thenReturn(patient);
            when(numberSequenceService.nextPatientRegNo()).thenReturn("SD-P0001");
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            patientService.create(request);

            verify(auditService).log(eq("CREATE"), eq("Patient"), isNull(), anyString());
            verify(auditService).log(eq("CREATE"), eq("Patient"), eq(1L), anyString());
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates patient successfully")
        void update_success() {
            PatientRequest request = validRequest();
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            PatientResponse result = patientService.update(1L, request);

            assertNotNull(result);
            verify(patientMapper).updateEntity(patient, request);
            verify(patientRepository).save(patient);
            verify(auditService).log(eq("UPDATE"), eq("Patient"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email owned by another")
        void update_duplicateEmail() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(patientRepository.existsByEmail("other@example.com")).thenReturn(true);
            PatientRequest request = validRequest();
            request.setEmail("other@example.com");

            assertThrows(DuplicateResourceException.class, () -> patientService.update(1L, request));
            verify(patientRepository, never()).save(any());
        }

        @Test
        @DisplayName("allows keeping same email")
        void update_sameEmail() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

            patientService.update(1L, validRequest());

            verify(patientRepository, never()).existsByEmail(anyString());
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("soft-deletes patient by deactivating")
        void delete_success() {
            when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
            when(patientRepository.save(any(Patient.class))).thenReturn(patient);

            patientService.delete(1L);

            assertFalse(patient.isActive());
            verify(patientRepository).save(patient);
            verify(auditService).log(eq("DELETE"), eq("Patient"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when patient not found")
        void delete_notFound() {
            when(patientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> patientService.delete(99L));
        }
    }

    @Nested
    @DisplayName("getTreatmentHistory()")
    class TreatmentHistory {

        @Test
        @DisplayName("returns patient appointments")
        void getTreatmentHistory_success() {
            Patient p = patient;
            Dentist d = Dentist.builder().id(1L).firstName("Jane").lastName("Smith").build();
            Treatment t = Treatment.builder().id(1L).name("Cleaning").code("TRT-C").build();
            Appointment a = Appointment.builder()
                    .id(1L).appointmentNumber("AP-2026-0001")
                    .patient(p).dentist(d).treatment(t)
                    .appointmentDate(LocalDate.now())
                    .startTime(LocalTime.of(9, 0))
                    .status(AppointmentStatus.COMPLETED)
                    .build();
            AppointmentResponse appointmentResponse = new AppointmentResponse(
                    1L, "AP-2026-0001", 1L, "John Doe", "0771234567",
                    1L, "Jane Smith", 1L, "Cleaning", "TRT-C",
                    LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(9, 30),
                    AppointmentStatus.COMPLETED, null, null, null);

            Page<Appointment> page = new PageImpl<>(List.of(a));
            when(appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(1L, Pageable.unpaged()))
                    .thenReturn(page);
            when(appointmentMapper.toResponse(a)).thenReturn(appointmentResponse);

            List<AppointmentResponse> result = patientService.getTreatmentHistory(1L);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("AP-2026-0001", result.get(0).appointmentNumber());
        }

        @Test
        @DisplayName("returns empty list when patient has no appointments")
        void getTreatmentHistory_empty() {
            Page<Appointment> empty = new PageImpl<>(List.of());
            when(appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(1L, Pageable.unpaged()))
                    .thenReturn(empty);

            List<AppointmentResponse> result = patientService.getTreatmentHistory(1L);

            assertTrue(result.isEmpty());
        }
    }
}
