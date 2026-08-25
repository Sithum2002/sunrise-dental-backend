package com.sunrise.dental.service.impl;

import com.sunrise.dental.audit.AuditService;
import com.sunrise.dental.dto.request.RegisterUserRequest;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.UserResponse;
import com.sunrise.dental.entity.User;
import com.sunrise.dental.exception.BusinessRuleException;
import com.sunrise.dental.exception.DuplicateResourceException;
import com.sunrise.dental.exception.ResourceNotFoundException;
import com.sunrise.dental.mapper.UserMapper;
import com.sunrise.dental.repository.UserRepository;
import com.sunrise.dental.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAll(Pageable pageable) {
        var page = userRepository.findAll(pageable);
        return new PageResponse<>(
                page.getContent().stream().map(userMapper::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userMapper.toResponse(findUser(id));
    }

    @Override
    @Transactional
    public UserResponse create(RegisterUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .contactNumber(request.getContactNumber())
                .role(request.getRole())
                .active(true)
                .build();
        userRepository.save(user);
        auditService.log("CREATE", "User", user.getId(), "Created user " + user.getUsername() + " with role " + user.getRole());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, RegisterUserRequest request) {
        User user = findUser(id);
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setContactNumber(request.getContactNumber());
        user.setRole(request.getRole());
        if (request.getPassword() != null && !request.getPassword().isBlank()
                && !request.getPassword().equals("__unchanged__")) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);
        auditService.log("UPDATE", "User", user.getId(), "Updated user " + user.getUsername());
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = findUser(id);
        if (user.getRole() == com.sunrise.dental.enums.Role.ADMIN
                && userRepository.countByActiveTrue() <= 1) {
            throw new BusinessRuleException("Cannot delete the last active administrator.");
        }
        user.setActive(false);
        userRepository.save(user);
        auditService.log("DELETE", "User", id, "Deactivated user " + user.getUsername());
    }

    @Override
    @Transactional
    public void toggleActive(Long id) {
        User user = findUser(id);
        if (user.isActive() && user.getRole() == com.sunrise.dental.enums.Role.ADMIN
                && userRepository.countByActiveTrue() <= 1) {
            throw new BusinessRuleException("Cannot deactivate the last active administrator.");
        }
        user.setActive(!user.isActive());
        userRepository.save(user);
        auditService.log("TOGGLE_ACTIVE", "User", id, "User " + user.getUsername() + " active=" + user.isActive());
    }

    @Override
    @Transactional
    public void resetFailedAttempts(Long id) {
        User user = findUser(id);
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        userRepository.save(user);
        auditService.log("UNLOCK", "User", id, "Unlocked user account " + user.getUsername());
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }
}
