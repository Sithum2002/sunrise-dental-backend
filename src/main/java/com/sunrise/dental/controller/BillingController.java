package com.sunrise.dental.controller;

import com.sunrise.dental.dto.request.BillRequest;
import com.sunrise.dental.dto.request.PaymentRequest;
import com.sunrise.dental.dto.response.ApiResponse;
import com.sunrise.dental.dto.response.BillResponse;
import com.sunrise.dental.dto.response.PageResponse;
import com.sunrise.dental.dto.response.PaymentResponse;
import com.sunrise.dental.service.BillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BillResponse>> createBill(@Valid @RequestBody BillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bill generated", billingService.createBill(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BillResponse>>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String paymentStatus,
            @PageableDefault(sort = "billedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Bills retrieved",
                billingService.getAll(from, to, patientId, paymentStatus, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved", billingService.getById(id)));
    }

    @GetMapping("/number/{billNumber}")
    public ResponseEntity<ApiResponse<BillResponse>> getByNumber(@PathVariable String billNumber) {
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved", billingService.getByBillNumber(billNumber)));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<PageResponse<BillResponse>>> getPatientBills(
            @PathVariable Long patientId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Patient bills retrieved",
                billingService.getPatientBills(patientId, pageable)));
    }

    @GetMapping("/{billId}/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPayments(@PathVariable Long billId) {
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", billingService.getPaymentsForBill(billId)));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PaymentResponse>> recordPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment recorded", billingService.recordPayment(request)));
    }
}
