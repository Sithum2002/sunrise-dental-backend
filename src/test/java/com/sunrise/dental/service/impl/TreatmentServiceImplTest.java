package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.request.TreatmentRequest;
import com.sunrise.dental.dto.response.TreatmentResponse;
import com.sunrise.dental.entity.Treatment;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.TreatmentMapper;
import com.sunrise.dental.repository.TreatmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentServiceImplTest {

    @Mock
    private TreatmentRepository treatmentRepository;
    @Mock
    private TreatmentMapper treatmentMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private TreatmentServiceImpl treatmentService;

    private Treatment treatment;
    private TreatmentResponse treatmentResponse;

    @BeforeEach
    void setUp() {
        treatment = Treatment.builder()
                .id(1L)
                .code("TRT-CLEAN")
                .name("Dental Cleaning")
                .description("Professional cleaning")
                .category("Preventive")
                .cost(5000.0)
                .durationMinutes(30)
                .active(true)
                .build();

        treatmentResponse = new TreatmentResponse(
                1L, "TRT-CLEAN", "Dental Cleaning", "Professional cleaning",
                "Preventive", 5000.0, 30, true);
    }

    private TreatmentRequest validRequest() {
        return TreatmentRequest.builder()
                .code("TRT-CLEAN")
                .name("Dental Cleaning")
                .description("Professional cleaning")
                .category("Preventive")
                .cost(5000.0)
                .durationMinutes(30)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("returns all treatments when onlyActive is false")
        void getAll_all() {
            when(treatmentRepository.findAll()).thenReturn(List.of(treatment));
            when(treatmentMapper.toResponse(treatment)).thenReturn(treatmentResponse);

            List<TreatmentResponse> result = treatmentService.getAll(false);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(treatmentRepository).findAll();
        }

        @Test
        @DisplayName("returns only active treatments when onlyActive is true")
        void getAll_active() {
            when(treatmentRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(treatment));
            when(treatmentMapper.toResponse(treatment)).thenReturn(treatmentResponse);

            List<TreatmentResponse> result = treatmentService.getAll(true);

            assertNotNull(result);
            assertEquals(1, result.size());
            verify(treatmentRepository).findByActiveTrueOrderByNameAsc();
        }

        @Test
        @DisplayName("returns empty list when no treatments match")
        void getAll_empty() {
            when(treatmentRepository.findAll()).thenReturn(List.of());

            List<TreatmentResponse> result = treatmentService.getAll(false);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns treatment by id")
        void getById_success() {
            when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));
            when(treatmentMapper.toResponse(treatment)).thenReturn(treatmentResponse);

            TreatmentResponse result = treatmentService.getById(1L);

            assertEquals("TRT-CLEAN", result.code());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when treatment not found")
        void getById_notFound() {
            when(treatmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> treatmentService.getById(99L));
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("creates treatment successfully")
        void create_success() {
            when(treatmentRepository.existsByCode("TRT-CLEAN")).thenReturn(false);
            when(treatmentMapper.toEntity(any(TreatmentRequest.class))).thenReturn(treatment);
            when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);
            when(treatmentMapper.toResponse(treatment)).thenReturn(treatmentResponse);

            TreatmentResponse result = treatmentService.create(validRequest());

            assertNotNull(result);
            assertEquals("TRT-CLEAN", result.code());
            verify(auditService).log(eq("CREATE"), eq("Treatment"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when code exists")
        void create_duplicateCode() {
            when(treatmentRepository.existsByCode("TRT-CLEAN")).thenReturn(true);

            assertThrows(DuplicateResourceException.class, () -> treatmentService.create(validRequest()));
            verify(treatmentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("updates treatment successfully")
        void update_success() {
            TreatmentRequest request = validRequest();
            when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));
            when(treatmentMapper.toResponse(treatment)).thenReturn(treatmentResponse);

            treatmentService.update(1L, request);

            verify(treatmentMapper).updateEntity(treatment, request);
            verify(treatmentRepository).save(treatment);
            verify(auditService).log(eq("UPDATE"), eq("Treatment"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when treatment not found")
        void update_notFound() {
            when(treatmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> treatmentService.update(99L, validRequest()));
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("soft-deletes treatment by setting inactive")
        void delete_success() {
            when(treatmentRepository.findById(1L)).thenReturn(Optional.of(treatment));
            when(treatmentRepository.save(any(Treatment.class))).thenReturn(treatment);

            treatmentService.delete(1L);

            assertFalse(treatment.isActive());
            verify(treatmentRepository).save(treatment);
            verify(auditService).log(eq("DELETE"), eq("Treatment"), eq(1L), anyString());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when treatment not found")
        void delete_notFound() {
            when(treatmentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> treatmentService.delete(99L));
        }
    }
}
