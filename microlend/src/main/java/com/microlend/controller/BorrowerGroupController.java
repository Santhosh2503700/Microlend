package com.microlend.controller;

import com.microlend.dto.request.BorrowerGroupRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.BorrowerGroup;
import com.microlend.service.BorrowerGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class BorrowerGroupController {

    private final BorrowerGroupService groupService;

    /** POST /api/groups */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<BorrowerGroup>> create(@Valid @RequestBody BorrowerGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Borrower group created", groupService.create(request)));
    }

    /** GET /api/groups */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<BorrowerGroup>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(groupService.getAll()));
    }

    /** GET /api/groups/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<BorrowerGroup>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getById(id)));
    }

    /** PUT /api/groups/{id} */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<BorrowerGroup>> update(
            @PathVariable Long id,
            @Valid @RequestBody BorrowerGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Group updated", groupService.update(id, request)));
    }

    /** GET /api/groups/centre/{centreID} */
    @GetMapping("/centre/{centreID}")
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<BorrowerGroup>>> getByCentre(@PathVariable Long centreID) {
        return ResponseEntity.ok(ApiResponse.success(groupService.getByCentre(centreID)));
    }
}
