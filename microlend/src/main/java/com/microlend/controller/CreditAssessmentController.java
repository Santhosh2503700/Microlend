package com.microlend.controller;

import com.microlend.dto.request.CreditAssessmentRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.CreditAssessment;
import com.microlend.service.CreditAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit-assessments")
@RequiredArgsConstructor
public class CreditAssessmentController {

    private final CreditAssessmentService assessmentService;

    /** POST /api/credit-assessments */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<CreditAssessment>> create(@Valid @RequestBody CreditAssessmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Credit assessment created", assessmentService.create(request)));
    }

    /** GET /api/credit-assessments */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<CreditAssessment>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(assessmentService.getAll()));
    }

    /** GET /api/credit-assessments/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<CreditAssessment>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(assessmentService.getById(id)));
    }

    /** PUT /api/credit-assessments/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<CreditAssessment>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreditAssessmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Assessment updated", assessmentService.update(id, request)));
    }
}
