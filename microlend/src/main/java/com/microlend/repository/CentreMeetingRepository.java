package com.microlend.repository;

import com.microlend.entity.CentreMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CentreMeetingRepository extends JpaRepository<CentreMeeting, Long> {
    List<CentreMeeting> findByCentreID(Long centreID);
    List<CentreMeeting> findByConductedByID(Long conductedByID);
}
