package com.sunrise.dental.service;

import com.sunrise.dental.dto.request.RegisterUserRequest;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PageResponse<UserResponse> getAll(Pageable pageable);

    UserResponse getById(Long id);

    UserResponse create(RegisterUserRequest request);

    UserResponse update(Long id, RegisterUserRequest request);

    void delete(Long id);

    void toggleActive(Long id);

    void resetFailedAttempts(Long id);
}
