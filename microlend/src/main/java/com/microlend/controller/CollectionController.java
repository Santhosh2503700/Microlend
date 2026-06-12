package com.microlend.controller;

import com.microlend.dto.request.CollectionRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.CollectionRecord;
import com.microlend.service.CollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /** POST /api/collections - Field Officer / Collections Officer records a payment */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','COLLECTIONS_OFFICER')")
    public ResponseEntity<ApiResponse<CollectionRecord>> record(@Valid @RequestBody CollectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Collection recorded", collectionService.recordCollection(request)));
    }

    /** GET /api/collections */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','COLLECTIONS_OFFICER')")
    public ResponseEntity<ApiResponse<List<CollectionRecord>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getAll()));
    }

    /** GET /api/collections/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','COLLECTIONS_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CollectionRecord>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getById(id)));
    }

    /** GET /api/collections/loan-account/{loanAccountID} */
    @GetMapping("/loan-account/{loanAccountID}")
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','COLLECTIONS_OFFICER','BRANCH_MANAGER','BORROWER')")
    public ResponseEntity<ApiResponse<List<CollectionRecord>>> getByLoanAccount(
            @PathVariable Long loanAccountID) {
        return ResponseEntity.ok(ApiResponse.success(collectionService.getByLoanAccount(loanAccountID)));
    }
}
