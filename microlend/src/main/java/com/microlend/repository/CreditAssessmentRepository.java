package com.microlend.repository;

import com.microlend.entity.CreditAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CreditAssessmentRepository extends JpaRepository<CreditAssessment, Long> {
}
