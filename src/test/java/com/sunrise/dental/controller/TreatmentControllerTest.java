package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.TreatmentRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.TreatmentResponse;
import com.sunrise.dental.service.TreatmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentControllerTest {

    @Mock
    private TreatmentService treatmentService;

    @InjectMocks
    private TreatmentController treatmentController;

    private TreatmentResponse treatmentResponse() {
        return new TreatmentResponse(
                1L, "TRT-CLEAN", "Dental Cleaning", "Professional cleaning",
                "Preventive", 5000.0, 30, true);
    }

    @Nested
    @DisplayName("GET /api/treatments")
    class GetAll {

        @Test
        @DisplayName("returns all treatments (onlyActive false)")
        void getAll_all() {
            when(treatmentService.getAll(false)).thenReturn(List.of(treatmentResponse()));

            ResponseEntity<ApiResponse<List<TreatmentResponse>>> result =
                    treatmentController.getAll(false);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().size());
            verify(treatmentService).getAll(false);
        }

        @Test
        @DisplayName("returns active treatments only")
        void getAll_active() {
            when(treatmentService.getAll(true)).thenReturn(List.of(treatmentResponse()));

            ResponseEntity<ApiResponse<List<TreatmentResponse>>> result =
                    treatmentController.getAll(true);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            verify(treatmentService).getAll(true);
        }

        @Test
        @DisplayName("defaults onlyActive to false")
        void getAll_default() {
            when(treatmentService.getAll(false)).thenReturn(Collections.emptyList());

            ResponseEntity<ApiResponse<List<TreatmentResponse>>> result =
                    treatmentController.getAll(false);

            assertTrue(result.getBody().getData().isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/treatments/{id}")
    class GetById {

        @Test
        @DisplayName("returns treatment by id")
        void getById_success() {
            when(treatmentService.getById(1L)).thenReturn(treatmentResponse());

            ResponseEntity<ApiResponse<TreatmentResponse>> result = treatmentController.getById(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("TRT-CLEAN", result.getBody().getData().code());
        }
    }

    @Nested
    @DisplayName("POST /api/treatments")
    class Create {

        @Test
        @DisplayName("creates treatment")
        void create_success() {
            TreatmentRequest request = TreatmentRequest.builder()
                    .code("TRT-CLEAN").name("Dental Cleaning").category("Preventive")
                    .cost(5000.0).durationMinutes(30).active(true)
                    .build();
            when(treatmentService.create(request)).thenReturn(treatmentResponse());

            ResponseEntity<ApiResponse<TreatmentResponse>> result = treatmentController.create(request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Treatment created", result.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("PUT /api/treatments/{id}")
    class Update {

        @Test
        @DisplayName("updates treatment")
        void update_success() {
            TreatmentRequest request = TreatmentRequest.builder()
                    .code("TRT-CLEAN").name("Dental Cleaning").category("Preventive")
                    .cost(5000.0).durationMinutes(30).active(true)
                    .build();
            when(treatmentService.update(1L, request)).thenReturn(treatmentResponse());

            ResponseEntity<ApiResponse<TreatmentResponse>> result = treatmentController.update(1L, request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Treatment updated", result.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("DELETE /api/treatments/{id}")
    class Delete {

        @Test
        @DisplayName("deletes treatment")
        void delete_success() {
            doNothing().when(treatmentService).delete(1L);

            ResponseEntity<ApiResponse<Void>> result = treatmentController.delete(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Treatment deactivated", result.getBody().getMessage());
            verify(treatmentService).delete(1L);
        }
    }
}
