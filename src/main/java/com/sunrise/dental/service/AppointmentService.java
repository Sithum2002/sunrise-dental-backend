package com.sunrise.dental.service;

import com.sunrise.dental.dto.request.AppointmentRequest;
import com.sunrise.dental.dto.request.RescheduleRequest;
import com.sunrise.dental.dto.response.AppointmentResponse;
import com.sunrise.dental.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AppointmentResponse register(AppointmentRequest request);

    AppointmentResponse getById(Long id);

    AppointmentResponse getByAppointmentNumber(String appointmentNumber);

    PageResponse<AppointmentResponse> getAll(LocalDate from, LocalDate to, Long dentistId,
                                             Long patientId, String status, Pageable pageable);

    AppointmentResponse reschedule(Long id, RescheduleRequest request);

    AppointmentResponse cancel(Long id, String reason);

    AppointmentResponse confirm(Long id);

    AppointmentResponse complete(Long id, String notes);

    AppointmentResponse markNoShow(Long id);

    List<AppointmentResponse> getTodayAppointments();

    List<AppointmentResponse> getUpcomingToday();

    List<AppointmentResponse> getByPatientId(Long patientId);
}
