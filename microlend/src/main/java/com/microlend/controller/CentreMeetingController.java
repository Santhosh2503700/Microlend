package com.microlend.controller;

import com.microlend.dto.request.CentreMeetingRequest;
import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.CentreMeeting;
import com.microlend.service.CentreMeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class CentreMeetingController {

    private final CentreMeetingService meetingService;

    /** POST /api/meetings - Schedule a meeting */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CentreMeeting>> create(@Valid @RequestBody CentreMeetingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Meeting scheduled", meetingService.create(request)));
    }

    /** GET /api/meetings */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<CentreMeeting>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getAll()));
    }

    /** GET /api/meetings/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<CentreMeeting>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getById(id)));
    }

    /** PUT /api/meetings/{id} - Record attendance and collections */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER')")
    public ResponseEntity<ApiResponse<CentreMeeting>> update(
            @PathVariable Long id,
            @Valid @RequestBody CentreMeetingRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Meeting updated", meetingService.update(id, request)));
    }

    /** GET /api/meetings/centre/{centreID} */
    @GetMapping("/centre/{centreID}")
    @PreAuthorize("hasAnyRole('ADMIN','FIELD_OFFICER','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<CentreMeeting>>> getByCentre(@PathVariable Long centreID) {
        return ResponseEntity.ok(ApiResponse.success(meetingService.getByCentre(centreID)));
    }
}
