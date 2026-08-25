package com.sunrise.dental.service;

import com.sunrise.dental.dto.request.DentistRequest;
import com.sunrise.dental.dto.response.DentistResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DentistService {

    List<DentistResponse> getAll(Pageable pageable);

    DentistResponse getById(Long id);

    DentistResponse create(DentistRequest request);

    DentistResponse update(Long id, DentistRequest request);

    void delete(Long id);

    void updateStatus(Long id, com.sunrise.dental.enums.DentistStatus status);

    List<DentistResponse> getAvailable();
}
