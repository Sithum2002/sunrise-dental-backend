package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.TreatmentRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.TreatmentResponse;
import com.sunrise.dental.service.TreatmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/treatments")
@RequiredArgsConstructor
public class TreatmentController {

    private final TreatmentService treatmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TreatmentResponse>>> getAll(
            @RequestParam(defaultValue = "false") boolean onlyActive) {
        return ResponseEntity.ok(ApiResponse.success("Treatments retrieved",
                treatmentService.getAll(onlyActive)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TreatmentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Treatment retrieved", treatmentService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TreatmentResponse>> create(@Valid @RequestBody TreatmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Treatment created", treatmentService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TreatmentResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody TreatmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Treatment updated", treatmentService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        treatmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Treatment deactivated", null));
    }
}
