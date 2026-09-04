package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.AppointmentRequest;
import com.sunrise.dental.dto.request.RescheduleRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.enums.AppointmentStatus;
import com.sunrise.dental.service.AppointmentService;
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
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private AppointmentResponse appointmentResponse() {
        return new AppointmentResponse(
                1L, "AP-2026-0001", 1L, "John Doe", "0771234567",
                2L, "Jane Smith", 3L, "Dental Cleaning", "TRT-CLEAN",
                LocalDate.now().plusDays(2), LocalTime.of(9, 0), LocalTime.of(9, 30),
                AppointmentStatus.SCHEDULED, null, null, null);
    }

    private AppointmentRequest appointmentRequest() {
        return AppointmentRequest.builder()
                .patientId(1L).dentistId(2L).treatmentId(3L)
                .appointmentDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(9, 0))
                .build();
    }

    @Nested
    @DisplayName("POST /api/appointments")
    class Register {

        @Test
        @DisplayName("registers appointment and returns success")
        void register_success() {
            AppointmentRequest request = appointmentRequest();
            AppointmentResponse response = appointmentResponse();
            when(appointmentService.register(request)).thenReturn(response);

            ResponseEntity<ApiResponse<AppointmentResponse>> result =
                    appointmentController.register(request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertTrue(result.getBody().isSuccess());
            assertEquals("Appointment registered", result.getBody().getMessage());
            assertEquals("AP-2026-0001", result.getBody().getData().appointmentNumber());
            verify(appointmentService).register(request);
        }
    }

    @Nested
    @DisplayName("GET /api/appointments")
    class GetAll {

        @Test
        @DisplayName("returns paginated appointments")
        void getAll_success() {
            PageResponse<AppointmentResponse> pageResponse = new PageResponse<>(
                    List.of(appointmentResponse()), 0, 10, 1, 1);
            when(appointmentService.getAll(any(), any(), any(), any(), any(), any()))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> result =
                    appointmentController.getAll(null, null, null, null, null, PageRequest.of(0, 10));

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertTrue(result.getBody().isSuccess());
            assertEquals(1, result.getBody().getData().content().size());
        }

        @Test
        @DisplayName("returns empty page when no appointments")
        void getAll_empty() {
            PageResponse<AppointmentResponse> pageResponse = new PageResponse<>(
                    Collections.emptyList(), 0, 10, 0, 0);
            when(appointmentService.getAll(any(), any(), any(), any(), any(), any()))
                    .thenReturn(pageResponse);

            ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> result =
                    appointmentController.getAll(null, null, null, null, null, PageRequest.of(0, 10));

            assertTrue(result.getBody().getData().content().isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/appointments/{id}")
    class GetById {

        @Test
        @DisplayName("returns appointment by id")
        void getById_success() {
            when(appointmentService.getById(1L)).thenReturn(appointmentResponse());

            ResponseEntity<ApiResponse<AppointmentResponse>> result = appointmentController.getById(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Appointment retrieved", result.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("GET /api/appointments/number/{appointmentNumber}")
    class GetByNumber {

        @Test
        @DisplayName("returns appointment by number")
        void getByNumber_success() {
            when(appointmentService.getByAppointmentNumber("AP-2026-0001"))
                    .thenReturn(appointmentResponse());

            ResponseEntity<ApiResponse<AppointmentResponse>> result =
                    appointmentController.getByNumber("AP-2026-0001");

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("AP-2026-0001", result.getBody().getData().appointmentNumber());
        }
    }

    @Nested
    @DisplayName("GET /api/appointments/today")
    class Today {

        @Test
        @DisplayName("returns today's appointments")
        void today_success() {
            when(appointmentService.getTodayAppointments()).thenReturn(List.of(appointmentResponse()));

            ResponseEntity<ApiResponse<List<AppointmentResponse>>> result =
                    appointmentController.today();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().size());
        }
    }

    @Nested
    @DisplayName("GET /api/appointments/upcoming")
    class Upcoming {

        @Test
        @DisplayName("returns upcoming appointments")
        void upcoming_success() {
            when(appointmentService.getUpcomingToday()).thenReturn(List.of(appointmentResponse()));

            ResponseEntity<ApiResponse<List<AppointmentResponse>>> result =
                    appointmentController.upcoming();

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(1, result.getBody().getData().size());
        }
    }

    @Nested
    @DisplayName("PATCH /api/appointments/{id}/reschedule")
    class Reschedule {

        @Test
        @DisplayName("reschedules appointment")
        void reschedule_success() {
            RescheduleRequest request = RescheduleRequest.builder()
                    .appointmentDate(LocalDate.now().plusDays(5))
                    .startTime(LocalTime.of(14, 0))
                    .build();
            when(appointmentService.reschedule(1L, request)).thenReturn(appointmentResponse());

            ResponseEntity<ApiResponse<AppointmentResponse>> result =
                    appointmentController.reschedule(1L, request);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Appointment rescheduled", result.getBody().getMessage());
            verify(appointmentService).reschedule(1L, request);
        }
    }

    @Nested
    @DisplayName("PATCH /api/appointments/{id}/cancel")
    class Cancel {

        @Test
        @DisplayName("cancels appointment")
        void cancel_success() {
            when(appointmentService.cancel(1L, "reason")).thenReturn(appointmentResponse());

            ResponseEntity<ApiResponse<AppointmentResponse>> result =
                    appointmentController.cancel(1L, "reason");

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Appointment cancelled", result.getBody().getMessage());
            verify(appointmentService).cancel(1L, "reason");
        }

        @Test
        @DisplayName("cancels appointment with null reason")
        void cancel_nullReason() {
            when(appointmentService.cancel(1L, null)).thenReturn(appointmentResponse());

            appointmentController.cancel(1L, null);

            verify(appointmentService).cancel(1L, null);
        }
    }

    @Nested
    @DisplayName("PATCH /api/appointments/{id}/confirm")
    class Confirm {

        @Test
        @DisplayName("confirms appointment")
        void confirm_success() {
            when(appointmentService.confirm(1L)).thenReturn(appointmentResponse());

            ResponseEntity<ApiResponse<AppointmentResponse>> result = appointmentController.confirm(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Appointment confirmed", result.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("PATCH /api/appointments/{id}/complete")
    class Complete {

        @Test
        @DisplayName("completes appointment")
        void complete_success() {
            when(appointmentService.complete(1L, "notes")).thenReturn(appointmentResponse());

            ResponseEntity<ApiResponse<AppointmentResponse>> result =
                    appointmentController.complete(1L, "notes");

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Appointment completed", result.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("PATCH /api/appointments/{id}/no-show")
    class NoShow {

        @Test
        @DisplayName("marks appointment as no-show")
        void noShow_success() {
            when(appointmentService.markNoShow(1L)).thenReturn(appointmentResponse());

            ResponseEntity<ApiResponse<AppointmentResponse>> result = appointmentController.noShow(1L);

            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals("Appointment marked as no-show", result.getBody().getMessage());
        }
    }
}
