package com.sunrise.dental.service;

import com.sunrise.dental.dto.request.TreatmentRequest;
import com.sunrise.dental.dto.response.TreatmentResponse;

import java.util.List;

public interface TreatmentService {

    List<TreatmentResponse> getAll(boolean onlyActive);

    TreatmentResponse getById(Long id);

    TreatmentResponse create(TreatmentRequest request);

    TreatmentResponse update(Long id, TreatmentRequest request);

    void delete(Long id);
}
