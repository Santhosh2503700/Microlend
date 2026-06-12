package com.microlend.controller;

import com.microlend.dto.request.CentreRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.Centre;
import com.microlend.service.CentreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/centres")
@RequiredArgsConstructor
public class CentreController {

    private final CentreService centreService;

    /** POST /api/centres */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Centre>> create(@Valid @RequestBody CentreRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Centre created", centreService.create(request)));
    }

    /** GET /api/centres */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<Centre>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(centreService.getAll()));
    }

    /** GET /api/centres/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Centre>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(centreService.getById(id)));
    }

    /** PUT /api/centres/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<Centre>> update(
            @PathVariable Long id,
            @Valid @RequestBody CentreRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Centre updated", centreService.update(id, request)));
    }
}
