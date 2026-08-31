package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.AppointmentRequest;
import com.sunrise.dental.dto.request.RescheduleRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<AppointmentResponse>> register(
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Appointment registered", appointmentService.register(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long dentistId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String status,
            @PageableDefault(sort = "appointmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Appointments retrieved",
                appointmentService.getAll(from, to, dentistId, patientId, status, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved", appointmentService.getById(id)));
    }

    @GetMapping("/number/{appointmentNumber}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getByNumber(@PathVariable String appointmentNumber) {
        return ResponseEntity.ok(ApiResponse.success("Appointment retrieved",
                appointmentService.getByAppointmentNumber(appointmentNumber)));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> today() {
        return ResponseEntity.ok(ApiResponse.success("Today's appointments", appointmentService.getTodayAppointments()));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> upcoming() {
        return ResponseEntity.ok(ApiResponse.success("Upcoming appointments", appointmentService.getUpcomingToday()));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<ApiResponse<AppointmentResponse>> reschedule(@PathVariable Long id,
                                                                       @Valid @RequestBody RescheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Appointment rescheduled", appointmentService.reschedule(id, request)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(@PathVariable Long id,
                                                                   @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("Appointment cancelled", appointmentService.cancel(id, reason)));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<AppointmentResponse>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appointment confirmed", appointmentService.confirm(id)));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<AppointmentResponse>> complete(@PathVariable Long id,
                                                                     @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(ApiResponse.success("Appointment completed", appointmentService.complete(id, notes)));
    }

    @PatchMapping("/{id}/no-show")
    public ResponseEntity<ApiResponse<AppointmentResponse>> noShow(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appointment marked as no-show", appointmentService.markNoShow(id)));
    }
}
