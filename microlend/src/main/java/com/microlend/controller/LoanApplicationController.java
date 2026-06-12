package com.microlend.controller;

import com.microlend.dto.request.LoanApplicationRequest;
import com.microlend.dto.request.LoanApplicationStatusRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.LoanApplication;
import com.microlend.enums.ApplicationStatus;
import com.microlend.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService applicationService;

    /** POST /api/loan-applications - Borrower or Credit Officer creates application */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BORROWER')")
    public ResponseEntity<ApiResponse<LoanApplication>> create(@Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan application created", applicationService.create(request)));
    }

    /** GET /api/loan-applications */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getAll()));
    }

    /** GET /api/loan-applications/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BORROWER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<LoanApplication>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getById(id)));
    }

    /** GET /api/loan-applications/borrower/{borrowerID} */
    @GetMapping("/borrower/{borrowerID}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BORROWER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getByBorrower(@PathVariable Long borrowerID) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getByBorrower(borrowerID)));
    }

    /** GET /api/loan-applications/status/{status} */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<LoanApplication>>> getByStatus(@PathVariable ApplicationStatus status) {
        return ResponseEntity.ok(ApiResponse.success(applicationService.getByStatus(status)));
    }

    /** PATCH /api/loan-applications/{id}/submit - Borrower submits DRAFT */
    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BORROWER')")
    public ResponseEntity<ApiResponse<LoanApplication>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Application submitted", applicationService.submit(id)));
    }

    /** PATCH /api/loan-applications/{id}/status - Credit Officer updates status */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<LoanApplication>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody LoanApplicationStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                applicationService.updateStatus(id, request)));
    }
}
