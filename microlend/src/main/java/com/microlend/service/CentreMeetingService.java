package com.microlend.service;

import com.microlend.dto.request.CentreMeetingRequest;
import com.microlend.entity.CentreMeeting;
import com.microlend.enums.MeetingStatus;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.CentreMeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CentreMeetingService {

    private final CentreMeetingRepository meetingRepository;

    public CentreMeeting create(CentreMeetingRequest req) {
        CentreMeeting meeting = CentreMeeting.builder()
                .centreID(req.getCentreID())
                .meetingDate(req.getMeetingDate() != null ? req.getMeetingDate() : LocalDate.now())
                .conductedByID(req.getConductedByID())
                .attendanceCount(req.getAttendanceCount() != null ? req.getAttendanceCount() : 0)
                .collectionAmount(req.getCollectionAmount() != null ? req.getCollectionAmount() : BigDecimal.ZERO)
                .status(req.getStatus() != null ? req.getStatus() : MeetingStatus.SCHEDULED)
                .build();
        return meetingRepository.save(meeting);
    }

    public List<CentreMeeting> getAll() {
        return meetingRepository.findAll();
    }

    public CentreMeeting getById(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found: " + id));
    }

    public CentreMeeting update(Long id, CentreMeetingRequest req) {
        CentreMeeting meeting = getById(id);
        if (req.getAttendanceCount() != null) meeting.setAttendanceCount(req.getAttendanceCount());
        if (req.getCollectionAmount() != null) meeting.setCollectionAmount(req.getCollectionAmount());
        if (req.getStatus() != null) meeting.setStatus(req.getStatus());
        return meetingRepository.save(meeting);
    }

    public List<CentreMeeting> getByCentre(Long centreID) {
        return meetingRepository.findByCentreID(centreID);
    }
}
