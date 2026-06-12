package com.microlend.controller;

import com.microlend.dto.request.DelinquencyCaseRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.DelinquencyCase;
import com.microlend.enums.DelinquencyStatus;
import com.microlend.scheduler.DelinquencyScheduler;
import com.microlend.service.DelinquencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DelinquencyController
 *
 * BUG FIX #5: Added POST /api/delinquency/trigger endpoint to allow ADMIN
 * to manually invoke the nightly delinquency engine (e.g. for testing or
 * catch-up after system downtime).
 *
 * The automatic run fires every night at 00:30 AM via @Scheduled in
 * DelinquencyScheduler. This endpoint is supplementary.
 */
@RestController
@RequestMapping("/api/delinquency")
@RequiredArgsConstructor
public class DelinquencyController {

    private final DelinquencyService delinquencyService;
    private final DelinquencyScheduler delinquencyScheduler;

    /** POST /api/delinquency - Manually open a delinquency case */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COLLECTIONS_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DelinquencyCase>> create(
            @Valid @RequestBody DelinquencyCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Delinquency case opened",
                        delinquencyService.create(request)));
    }

    /** GET /api/delinquency */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COLLECTIONS_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<DelinquencyCase>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(delinquencyService.getAll()));
    }

    /** GET /api/delinquency/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COLLECTIONS_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<DelinquencyCase>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(delinquencyService.getById(id)));
    }

    /** PUT /api/delinquency/{id} - Update action/status */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COLLECTIONS_OFFICER')")
    public ResponseEntity<ApiResponse<DelinquencyCase>> update(
            @PathVariable Long id,
            @Valid @RequestBody DelinquencyCaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Case updated",
                delinquencyService.update(id, request)));
    }

    /** GET /api/delinquency/status/{status} */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','COLLECTIONS_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<DelinquencyCase>>> getByStatus(
            @PathVariable DelinquencyStatus status) {
        return ResponseEntity.ok(ApiResponse.success(delinquencyService.getByStatus(status)));
    }

    /** GET /api/delinquency/officer/{officerID} */
    @GetMapping("/officer/{officerID}")
    @PreAuthorize("hasAnyRole('ADMIN','COLLECTIONS_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<DelinquencyCase>>> getByOfficer(
            @PathVariable Long officerID) {
        return ResponseEntity.ok(ApiResponse.success(delinquencyService.getByOfficer(officerID)));
    }

    /** GET /api/delinquency/loan-account/{loanAccountID} */
    @GetMapping("/loan-account/{loanAccountID}")
    @PreAuthorize("hasAnyRole('ADMIN','COLLECTIONS_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<DelinquencyCase>>> getByLoanAccount(
            @PathVariable Long loanAccountID) {
        return ResponseEntity.ok(ApiResponse.success(
                delinquencyService.getByLoanAccount(loanAccountID)));
    }

    /**
     * BUG FIX #5: Manual trigger for the nightly delinquency engine.
     * POST /api/delinquency/trigger — ADMIN only.
     * The scheduler also runs automatically every night at 00:30 AM.
     */
    @PostMapping("/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> triggerManualRun() {
        delinquencyScheduler.triggerManualRun();
        return ResponseEntity.ok(ApiResponse.success(
                "Delinquency engine triggered manually.",
                "Automatic run also fires nightly at 00:30 AM via @Scheduled cron."));
    }
}
