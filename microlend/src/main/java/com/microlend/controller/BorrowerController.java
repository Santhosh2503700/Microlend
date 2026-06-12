package com.microlend.controller;

import com.microlend.dto.request.BorrowerRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.Borrower;
import com.microlend.enums.BorrowerStatus;
import com.microlend.service.BorrowerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrowers")
@RequiredArgsConstructor
public class BorrowerController {

    private final BorrowerService borrowerService;

    /** POST /api/borrowers - Register new borrower (Credit Officer / Admin) */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','FIELD_OFFICER')")
    public ResponseEntity<ApiResponse<Borrower>> create(@Valid @RequestBody BorrowerRequest request) {
        Borrower borrower = borrowerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Borrower registered successfully", borrower));
    }

    /** GET /api/borrowers */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<Borrower>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(borrowerService.getAll()));
    }

    /** GET /api/borrowers/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Borrower>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(borrowerService.getById(id)));
    }

    /** PUT /api/borrowers/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER')")
    public ResponseEntity<ApiResponse<Borrower>> update(
            @PathVariable Long id,
            @Valid @RequestBody BorrowerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Borrower updated", borrowerService.update(id, request)));
    }

    /** DELETE /api/borrowers/{id} */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        borrowerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Borrower deleted", null));
    }

    /** GET /api/borrowers/status/{status} */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','CREDIT_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<Borrower>>> getByStatus(@PathVariable BorrowerStatus status) {
        return ResponseEntity.ok(ApiResponse.success(borrowerService.getByStatus(status)));
    }
}
