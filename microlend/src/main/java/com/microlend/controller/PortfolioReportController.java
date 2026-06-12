package com.microlend.controller;

import com.microlend.dto.response.ApiResponse;
import com.microlend.entity.PortfolioReport;
import com.microlend.enums.ReportScope;
import com.microlend.service.PortfolioReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class PortfolioReportController {

    private final PortfolioReportService reportService;

    /** POST /api/reports/generate?scope=BRANCH&scopeRefID=1 */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<PortfolioReport>> generate(
            @RequestParam ReportScope scope,
            @RequestParam(required = false) Long scopeRefID) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report generated", reportService.generate(scope, scopeRefID)));
    }

    /** GET /api/reports */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<List<PortfolioReport>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(reportService.getAll()));
    }

    /** GET /api/reports/{id} */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<ApiResponse<PortfolioReport>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getById(id)));
    }
}
