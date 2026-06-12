package com.microlend.controller;

import com.microlend.dto.request.LoanProductRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.LoanProduct;
import com.microlend.service.LoanProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-products")
@RequiredArgsConstructor
public class LoanProductController {

    private final LoanProductService productService;

    /** POST /api/loan-products - Admin creates a product */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanProduct>> create(@Valid @RequestBody LoanProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Loan product created", productService.create(request)));
    }

    /** GET /api/loan-products - All roles can view active products */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanProduct>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAll()));
    }

    /** GET /api/loan-products/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanProduct>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    /** PUT /api/loan-products/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanProduct>> update(
            @PathVariable Long id,
            @Valid @RequestBody LoanProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Product updated", productService.update(id, request)));
    }

    /** PATCH /api/loan-products/{id}/discontinue */
    @PatchMapping("/{id}/discontinue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> discontinue(@PathVariable Long id) {
        productService.discontinue(id);
        return ResponseEntity.ok(ApiResponse.success("Product discontinued", null));
    }
}
