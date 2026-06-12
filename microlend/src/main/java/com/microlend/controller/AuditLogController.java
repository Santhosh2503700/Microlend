package com.microlend.controller;

import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.AuditLog;
import com.microlend.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /** GET /api/admin/audit-logs - Admin views all audit logs */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(auditLogService.getAllLogs()));
    }
}
