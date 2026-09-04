package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.request.DentistRequest;
import com.sunrise.dental.dto.response.DentistResponse;
import com.sunrise.dental.entity.Dentist;
import com.sunrise.dental.enums.DentistStatus;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.DentistMapper;
import com.sunrise.dental.repository.DentistRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DentistServiceImplTest {

    @Mock
    private DentistRepository dentistRepository;
    @Mock
    private DentistMapper dentistMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private DentistServiceImpl dentistService;

    private Dentist dentist;
    private DentistResponse dentistResponse;

    @BeforeEach
    void setUp() {
        dentist = Dentist.builder()
                .id(1L)
                .licenceNo("DR-0001")
                .firstName("Jane")
                .lastName("Smith")
                .specialization("Orthodontics")
                .contactNumber("0771234567")
                .email("jane.smith@example.com")
                .status(DentistStatus.AVAILABLE)
                .yearsOfExperience(10)
                .build();

        dentistResponse = new DentistResponse(
                1L, "DR-0001", "Jane", "Smith", "Jane Smith",
                "Orthodontics", "0771234567", "jane.smith@example.com",
                DentistStatus.AVAILABLE, 10, LocalDate.of(2020, 1, 1), null);
    }

    private DentistRequest validRequest() {
        return DentistRequest.builder()
                .licenceNo("DR-0001")
                .firstName("Jane")
                .lastName("Smith")
                .specialization("Orthodontics")
                .contactNumber("0771234567")
                .email("jane.smith@example.com")
                .status(DentistStatus.AVAILABLE)
                .yearsOfExperience(10)
                .joiningDate(LocalDate.of(2020, 1, 1))
                .build();
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("returns paginated dentists")
        void getAll_success() {
            Page<Dentist> page = new PageImpl<>(List.of(dentist));
            Pageable pageable = PageRequest.of(0, 10);
            when(dentistRepository.findAll(pageable)).thenReturn(page);
            when(dentistMapper.toResponse(dentist)).thenReturn(dentistResponse);

            List<DentistResponse> result = dentistService.getAll(pageable);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Jane Smith", result.get(0).fullName());
        }

        @Test
        @DisplayName("returns empty list when no dentists")
        void getAll_empty() {
            Pageable pageable = PageRequest.of(0, 10);
            when(dentistRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

            List<DentistResponse> result = dentistService.getAll(pageable);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns dentist by id")
        void getById_success() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
            when(dentistMapper.toResponse(dentist)).thenReturn(dentistResponse);

            DentistResponse result = dentistService.getById(1L);

            assertEquals("DR-0001", result.licenceNo());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when dentist not found")
        void getById_notFound() {
            when(dentistRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> dentistService.getById(99L));
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("creates dentist successfully")
        void create_success() {
            when(dentistRepository.existsByLicenceNo("DR-0001")).thenReturn(false);
            when(dentistRepository.existsByEmail("jane.smith@example.com")).thenReturn(false);
            when(dentistMapper.toEntity(any(DentistRequest.class))).thenReturn(dentist);
            when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);
            when(dentistMapper.toResponse(dentist)).thenReturn(dentistResponse);

            DentistResponse result = dentistService.create(validRequest());

            assertNotNull(result);
            assertEquals("DR-0001", result.licenceNo());
            verify(auditService).log(eq("CREATE"), eq("Dentist"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when licence exists")
        void create_duplicateLicence() {
            when(dentistRepository.existsByLicenceNo("DR-0001")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> dentistService.create(validRequest()));
            verify(dentistRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email exists")
        void create_duplicateEmail() {
            when(dentistRepository.existsByLicenceNo("DR-0001")).thenReturn(false);
            when(dentistRepository.existsByEmail("jane.smith@example.com")).thenReturn(true);

            DuplicateResourceException ex = assertThrows(DuplicateResourceException.class,
                    () -> dentistService.create(validRequest()));
            assertTrue(ex.getMessage().contains("Email already registered"));
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates dentist successfully")
        void update_success() {
            DentistRequest request = validRequest();
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
            when(dentistMapper.toResponse(dentist)).thenReturn(dentistResponse);

            dentistService.update(1L, request);

            verify(dentistMapper).updateEntity(dentist, request);
            verify(dentistRepository).save(dentist);
            verify(auditService).log(eq("UPDATE"), eq("Dentist"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when email owned by another")
        void update_duplicateEmail() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
            when(dentistRepository.existsByEmail("other@example.com")).thenReturn(true);
            DentistRequest request = validRequest();
            request.setEmail("other@example.com");

            assertThrows(DuplicateResourceException.class, () -> dentistService.update(1L, request));
        }

        @Test
        @DisplayName("allows same email")
        void update_sameEmail() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));

            dentistService.update(1L, validRequest());

            verify(dentistRepository, never()).existsByEmail(anyString());
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("soft-deletes dentist by marking unavailable")
        void delete_success() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
            when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);

            dentistService.delete(1L);

            assertEquals(DentistStatus.UNAVAILABLE, dentist.getStatus());
            verify(auditService).log(eq("DELETE"), eq("Dentist"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when dentist not found")
        void delete_notFound() {
            when(dentistRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> dentistService.delete(99L));
        }
    }

    @Nested
    @DisplayName("updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("updates dentist status")
        void updateStatus_success() {
            when(dentistRepository.findById(1L)).thenReturn(Optional.of(dentist));
            when(dentistRepository.save(any(Dentist.class))).thenReturn(dentist);

            dentistService.updateStatus(1L, DentistStatus.ON_LEAVE);

            assertEquals(DentistStatus.ON_LEAVE, dentist.getStatus());
            verify(auditService).log(eq("UPDATE_STATUS"), eq("Dentist"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when dentist not found")
        void updateStatus_notFound() {
            when(dentistRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> dentistService.updateStatus(99L, DentistStatus.AVAILABLE));
        }
    }

    @Nested
    @DisplayName("getAvailable()")
    class GetAvailable {

        @Test
        @DisplayName("returns only available dentists")
        void getAvailable_success() {
            when(dentistRepository.findByStatus(DentistStatus.AVAILABLE)).thenReturn(List.of(dentist));
            when(dentistMapper.toResponse(dentist)).thenReturn(dentistResponse);

            List<DentistResponse> result = dentistService.getAvailable();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(DentistStatus.AVAILABLE, result.get(0).status());
        }

        @Test
        @DisplayName("returns empty list when no available dentists")
        void getAvailable_empty() {
            when(dentistRepository.findByStatus(DentistStatus.AVAILABLE)).thenReturn(List.of());

            List<DentistResponse> result = dentistService.getAvailable();

            assertTrue(result.isEmpty());
        }
    }
}
