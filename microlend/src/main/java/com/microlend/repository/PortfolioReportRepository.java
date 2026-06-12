package com.microlend.repository;

import com.microlend.entity.PortfolioReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioReportRepository extends JpaRepository<PortfolioReport, Long> {
}
