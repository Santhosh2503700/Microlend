package com.microlend.service;

import com.microlend.dto.request.CentreRequest;
import com.microlend.entity.Centre;
import com.microlend.enums.CentreStatus;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.CentreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CentreService {

    private final CentreRepository centreRepository;

    public Centre create(CentreRequest req) {
        Centre centre = Centre.builder()
                .centreName(req.getCentreName())
                .branchID(req.getBranchID())
                .fieldOfficerID(req.getFieldOfficerID())
                .village(req.getVillage())
                .meetingDay(req.getMeetingDay())
                .meetingTime(req.getMeetingTime())
                .status(req.getStatus() != null ? req.getStatus() : CentreStatus.ACTIVE)
                .build();
        return centreRepository.save(centre);
    }

    public List<Centre> getAll() {
        return centreRepository.findAll();
    }

    public Centre getById(Long id) {
        return centreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Centre not found: " + id));
    }

    public Centre update(Long id, CentreRequest req) {
        Centre centre = getById(id);
        centre.setCentreName(req.getCentreName());
        if (req.getFieldOfficerID() != null) centre.setFieldOfficerID(req.getFieldOfficerID());
        if (req.getMeetingDay() != null) centre.setMeetingDay(req.getMeetingDay());
        if (req.getMeetingTime() != null) centre.setMeetingTime(req.getMeetingTime());
        if (req.getStatus() != null) centre.setStatus(req.getStatus());
        return centreRepository.save(centre);
    }
}
