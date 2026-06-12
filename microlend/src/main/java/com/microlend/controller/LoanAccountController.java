package com.microlend.controller;

import com.microlend.dto.request.DisbursementRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.LoanAccount;
import com.microlend.entity.RepaymentSchedule;
import com.microlend.service.LoanDisbursementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-accounts")
@RequiredArgsConstructor
public class LoanAccountController {

    private final LoanDisbursementService disbursementService;

    /**
     * POST /api/loan-accounts/disburse
     * Credit Officer disburses the loan — creates LoanAccount + RepaymentSchedule
     */
    @PostMapping("/disburse")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<LoanAccount>> disburse(@Valid @RequestBody DisbursementRequest request) {
        LoanAccount account = disbursementService.disburse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan disbursed successfully. Repayment schedule generated.", account));
    }

    /** GET /api/loan-accounts */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BRANCH_MANAGER','COLLECTIONS_OFFICER')")
    public ResponseEntity<ApiResponse<List<LoanAccount>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(disbursementService.getAll()));
    }

    /** GET /api/loan-accounts/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BORROWER','BRANCH_MANAGER','COLLECTIONS_OFFICER')")
    public ResponseEntity<ApiResponse<LoanAccount>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(disbursementService.getById(id)));
    }

    /** GET /api/loan-accounts/borrower/{borrowerID} */
    @GetMapping("/borrower/{borrowerID}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BORROWER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<LoanAccount>>> getByBorrower(@PathVariable Long borrowerID) {
        return ResponseEntity.ok(ApiResponse.success(disbursementService.getByBorrower(borrowerID)));
    }

    /** GET /api/loan-accounts/{id}/schedule - View repayment schedule */
    @GetMapping("/{id}/schedule")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BORROWER','FIELD_OFFICER','COLLECTIONS_OFFICER')")
    public ResponseEntity<ApiResponse<List<RepaymentSchedule>>> getSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(disbursementService.getSchedule(id)));
    }
}
