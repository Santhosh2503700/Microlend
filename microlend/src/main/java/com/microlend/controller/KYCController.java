package com.microlend.controller;

import com.microlend.dto.request.BorrowerKYCRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.BorrowerKYC;
import com.microlend.enums.KYCStatus;
import com.microlend.service.BorrowerKYCService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * KYCController
 *
 * BUG FIX #3: FIELD_OFFICER is no longer allowed to call PATCH /api/kyc/{id}/verify.
 *
 * ORIGINAL BUG:
 *   SecurityConfig had: .requestMatchers("/api/kyc/**").hasAnyRole("ADMIN","CREDIT_OFFICER","FIELD_OFFICER")
 *   This wildcard gave FIELD_OFFICER full access to every KYC endpoint including
 *   /verify — the same person who uploads a borrower's documents could approve
 *   them (maker-checker fraud risk).
 *
 * FIX:
 *   POST /api/kyc           → ADMIN, CREDIT_OFFICER, FIELD_OFFICER  (upload — field work)
 *   PATCH /api/kyc/{id}/verify → ADMIN, CREDIT_OFFICER ONLY
 *   GET   /api/kyc/**       → ADMIN, CREDIT_OFFICER, BRANCH_MANAGER
 *
 *   A FIELD_OFFICER calling PATCH /api/kyc/{id}/verify → 403 Forbidden.
 *   Defence-in-depth: BorrowerKYCService.updateStatus() also rejects FIELD_OFFICER.
 */
@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KYCController {

    private final BorrowerKYCService kycService;

    /**
     * Upload a KYC document — FIELD_OFFICER allowed (this is their job in the field).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','FIELD_OFFICER')")
    public ResponseEntity<ApiResponse<BorrowerKYC>> create(
            @Valid @RequestBody BorrowerKYCRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("KYC document uploaded", kycService.create(request)));
    }

    /** GET /api/kyc/borrower/{borrowerID} */
    @GetMapping("/borrower/{borrowerID}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<BorrowerKYC>>> getByBorrower(
            @PathVariable Long borrowerID) {
        return ResponseEntity.ok(ApiResponse.success(kycService.getByBorrower(borrowerID)));
    }

    /** GET /api/kyc/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<BorrowerKYC>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(kycService.getById(id)));
    }

    /** GET /api/kyc */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<List<BorrowerKYC>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(kycService.getAll()));
    }

    /**
     * BUG FIX #3: Verify or reject KYC — ADMIN and CREDIT_OFFICER ONLY.
     * FIELD_OFFICER is explicitly excluded. Attempting to call this → 403 Forbidden.
     *
     * This @PreAuthorize works in concert with the route-level rule in SecurityConfig:
     *   PATCH /api/kyc/{id}/verify → hasAnyRole("ADMIN","CREDIT_OFFICER")
     */
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<BorrowerKYC>> updateStatus(
            @PathVariable Long id,
            @RequestParam KYCStatus status,
            @RequestParam Long verifiedByID) {
        return ResponseEntity.ok(ApiResponse.success("KYC status updated",
                kycService.updateStatus(id, status, verifiedByID)));
    }
}
