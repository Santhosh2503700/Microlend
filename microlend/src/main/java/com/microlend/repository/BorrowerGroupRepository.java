package com.microlend.repository;

import com.microlend.entity.BorrowerGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BorrowerGroupRepository extends JpaRepository<BorrowerGroup, Long> {
    List<BorrowerGroup> findByCentreID(Long centreID);
    List<BorrowerGroup> findByFieldOfficerID(Long fieldOfficerID);
}
