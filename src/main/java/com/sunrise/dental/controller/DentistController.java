package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.DentistRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.DentistResponse;
import com.sunrise.dental.enums.DentistStatus;
import com.sunrise.dental.service.DentistService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dentists")
@RequiredArgsConstructor
public class DentistController {

    private final DentistService dentistService;

    @Operation(summary = "List dentists (role based access)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<DentistResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Dentists retrieved", dentistService.getAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DentistResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Dentist retrieved", dentistService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DentistResponse>> create(@Valid @RequestBody DentistRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Dentist added", dentistService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DentistResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody DentistRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Dentist updated", dentistService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        dentistService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Dentist removed", null));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Long id,
                                                          @RequestParam DentistStatus status) {
        dentistService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Dentist status updated to " + status, null));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<DentistResponse>>> getAvailable() {
        return ResponseEntity.ok(ApiResponse.success("Available dentists retrieved", dentistService.getAvailable()));
    }
}
