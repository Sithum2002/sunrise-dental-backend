package com.sunrise.dental.service;

import com.sunrise.dental.dto.request.PatientRequest;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.PatientResponse;
import org.springframework.data.domain.Pageable;

public interface PatientService {

    PageResponse<PatientResponse> getAll(Pageable pageable, String search, String gender, String bloodGroup);

    PatientResponse getById(Long id);

    PatientResponse getByRegNo(String regNo);

    PatientResponse create(PatientRequest request);

    PatientResponse update(Long id, PatientRequest request);

    void delete(Long id);

    java.util.List<com.sunrise.dental.dto.response.AppointmentResponse> getTreatmentHistory(Long patientId);
}
