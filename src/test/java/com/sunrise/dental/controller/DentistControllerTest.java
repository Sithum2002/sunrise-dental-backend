package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.DentistRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.DentistResponse;
import com.sunrise.dental.enums.DentistStatus;
import com.sunrise.dental.service.DentistService;
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
class DentistControllerTest {

    @Mock
    private DentistService dentistService;

    @InjectMocks
    private DentistController dentistController;

    private DentistResponse dentistResponse() {
        return new DentistResponse(
                1L, "DR-0001", "Jane", "Smith", "Jane Smith",
                "Orthodontics", "0771234567", "jane@example.com",
                DentistStatus.AVAILABLE, 10, LocalDate.of(2020, 1, 1), null);
    }

    @Nested
    @DisplayName("GET /api/admin/dentists")
    class GetAll {

        @Test
        @DisplayName("returns list of dentists")
        void getAll_success() {
            Pageable pageable = PageRequest.of(0, 10);
            when(dentistService.getAll(pageable)).thenReturn(List.of(dentistResponse()));

            ResponseEntity<ApiResponse<List<DentistResponse>>> result =
                    dentistController.getAll(pageable);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().size());
        }
    }

    @Nested
    @DisplayName("GET /api/admin/dentists/{id}")
    class GetById {

        @Test
        @DisplayName("returns dentist by id")
        void getById_success() {
            when(dentistService.getById(1L)).thenReturn(dentistResponse());

            ResponseEntity<ApiResponse<DentistResponse>> result = dentistController.getById(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("DR-0001", result.getBody().getData().licenceNo());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/dentists")
    class Create {

        @Test
        @DisplayName("creates dentist")
        void create_success() {
            DentistRequest request = DentistRequest.builder()
                    .licenceNo("DR-0001").firstName("Jane").lastName("Smith")
                    .specialization("Orthodontics").contactNumber("0771234567")
                    .email("jane@example.com").status(DentistStatus.AVAILABLE)
                    .build();
            when(dentistService.create(request)).thenReturn(dentistResponse());

            ResponseEntity<ApiResponse<DentistResponse>> result = dentistController.create(request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Dentist added", result.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/dentists/{id}")
    class Update {

        @Test
        @DisplayName("updates dentist")
        void update_success() {
            DentistRequest request = DentistRequest.builder()
                    .licenceNo("DR-0001").firstName("Jane").lastName("Smith")
                    .specialization("Orthodontics").contactNumber("0771234567")
                    .email("jane@example.com").status(DentistStatus.AVAILABLE)
                    .build();
            when(dentistService.update(1L, request)).thenReturn(dentistResponse());

            ResponseEntity<ApiResponse<DentistResponse>> result = dentistController.update(1L, request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Dentist updated", result.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/dentists/{id}")
    class Delete {

        @Test
        @DisplayName("deletes dentist")
        void delete_success() {
            doNothing().when(dentistService).delete(1L);

            ResponseEntity<ApiResponse<Void>> result = dentistController.delete(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Dentist removed", result.getBody().getMessage());
            verify(dentistService).delete(1L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/admin/dentists/{id}/status")
    class UpdateStatus {

        @Test
        @DisplayName("updates dentist status")
        void updateStatus_success() {
            doNothing().when(dentistService).updateStatus(1L, DentistStatus.ON_LEAVE);

            ResponseEntity<ApiResponse<Void>> result =
                    dentistController.updateStatus(1L, DentistStatus.ON_LEAVE);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(dentistService).updateStatus(1L, DentistStatus.ON_LEAVE);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/dentists/available")
    class GetAvailable {

        @Test
        @DisplayName("returns available dentists")
        void getAvailable_success() {
            when(dentistService.getAvailable()).thenReturn(List.of(dentistResponse()));

            ResponseEntity<ApiResponse<List<DentistResponse>>> result =
                    dentistController.getAvailable();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().size());
        }

        @Test
        @DisplayName("returns empty when none available")
        void getAvailable_empty() {
            when(dentistService.getAvailable()).thenReturn(Collections.emptyList());

            ResponseEntity<ApiResponse<List<DentistResponse>>> result =
                    dentistController.getAvailable();

            assertTrue(result.getBody().getData().isEmpty());
        }
    }
}
