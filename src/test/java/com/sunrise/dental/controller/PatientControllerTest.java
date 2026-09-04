package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.PatientRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.PatientResponse;
import com.sunrise.dental.enums.BloodGroup;
import com.sunrise.dental.enums.Gender;
import com.sunrise.dental.service.PatientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientControllerTest {

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientController patientController;

    private PatientResponse patientResponse() {
        return new PatientResponse(
                1L, "SD-P0001", "John", "Doe", "John Doe",
                "123 Main St", "0771234567", "john.doe@example.com",
                LocalDate.of(1990, 1, 1), Gender.MALE, BloodGroup.O_POSITIVE,
                null, null, null, true, null);
    }

    private PatientRequest patientRequest() {
        return PatientRequest.builder()
                .firstName("John").lastName("Doe")
                .address("123 Main St")
                .contactNumber("0771234567")
                .email("john.doe@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .build();
    }

    @Nested
    @DisplayName("GET /api/patients")
    class GetAll {

        @Test
        @DisplayName("returns paginated patients")
        void getAll_success() {
            PageResponse<PatientResponse> pageResponse = new PageResponse<>(
                    List.of(patientResponse()), 0, 10, 1, 1);
            when(patientService.getAll(any(Pageable.class), any(), any(), any()))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<PatientResponse>>> result =
                    patientController.getAll(null, null, null, PageRequest.of(0, 10));

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().content().size());
        }
    }

    @Nested
    @DisplayName("GET /api/patients/{id}")
    class GetById {

        @Test
        @DisplayName("returns patient by id")
        void getById_success() {
            when(patientService.getById(1L)).thenReturn(patientResponse());

            ResponseEntity<ApiResponse<PatientResponse>> result = patientController.getById(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("SD-P0001", result.getBody().getData().regNo());
        }
    }

    @Nested
    @DisplayName("GET /api/patients/reg-no/{regNo}")
    class GetByRegNo {

        @Test
        @DisplayName("returns patient by reg no")
        void getByRegNo_success() {
            when(patientService.getByRegNo("SD-P0001")).thenReturn(patientResponse());

            ResponseEntity<ApiResponse<PatientResponse>> result =
                    patientController.getByRegNo("SD-P0001");

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("John Doe", result.getBody().getData().fullName());
        }
    }

    @Nested
    @DisplayName("POST /api/patients")
    class Create {

        @Test
        @DisplayName("creates patient")
        void create_success() {
            PatientRequest request = patientRequest();
            when(patientService.create(request)).thenReturn(patientResponse());

            ResponseEntity<ApiResponse<PatientResponse>> result = patientController.create(request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Patient registered", result.getBody().getMessage());
            verify(patientService).create(request);
        }
    }

    @Nested
    @DisplayName("PUT /api/patients/{id}")
    class Update {

        @Test
        @DisplayName("updates patient")
        void update_success() {
            PatientRequest request = patientRequest();
            when(patientService.update(1L, request)).thenReturn(patientResponse());

            ResponseEntity<ApiResponse<PatientResponse>> result =
                    patientController.update(1L, request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Patient updated", result.getBody().getMessage());
            verify(patientService).update(1L, request);
        }
    }

    @Nested
    @DisplayName("DELETE /api/patients/{id}")
    class Delete {

        @Test
        @DisplayName("deletes patient")
        void delete_success() {
            doNothing().when(patientService).delete(1L);

            ResponseEntity<ApiResponse<Void>> result = patientController.delete(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Patient deactivated", result.getBody().getMessage());
            verify(patientService).delete(1L);
        }
    }

    @Nested
    @DisplayName("GET /api/patients/{id}/history")
    class History {

        @Test
        @DisplayName("returns treatment history")
        void history_success() {
            when(patientService.getTreatmentHistory(1L))
                    .thenReturn(Collections.emptyList());

            ResponseEntity<ApiResponse<List<AppointmentResponse>>> result =
                    patientController.history(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertTrue(result.getBody().getData().isEmpty());
        }
    }
}
