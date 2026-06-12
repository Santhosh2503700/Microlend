package com.microlend.controller;

import com.microlend.dto.request.SanctionLetterRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.SanctionLetter;
import com.microlend.service.SanctionLetterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanction-letters")
@RequiredArgsConstructor
public class SanctionLetterController {

    private final SanctionLetterService sanctionService;

    /** POST /api/sanction-letters - Credit Officer issues sanction */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<SanctionLetter>> issue(@Valid @RequestBody SanctionLetterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sanction letter issued", sanctionService.issue(request)));
    }

    /** GET /api/sanction-letters */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<SanctionLetter>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(sanctionService.getAll()));
    }

    /** GET /api/sanction-letters/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BORROWER')")
    public ResponseEntity<ApiResponse<SanctionLetter>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(sanctionService.getById(id)));
    }

    /** GET /api/sanction-letters/application/{applicationID} */
    @GetMapping("/application/{applicationID}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BORROWER')")
    public ResponseEntity<ApiResponse<SanctionLetter>> getByApplicationId(@PathVariable Long applicationID) {
        return ResponseEntity.ok(ApiResponse.success(sanctionService.getByApplicationId(applicationID)));
    }

    /** PATCH /api/sanction-letters/{id}/accept - Borrower accepts sanction */
    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN','BORROWER','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<SanctionLetter>> accept(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Sanction letter accepted", sanctionService.accept(id)));
    }
}
