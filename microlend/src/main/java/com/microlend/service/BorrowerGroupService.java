package com.microlend.service;

import com.microlend.dto.request.BorrowerGroupRequest;
import com.microlend.entity.BorrowerGroup;
import com.microlend.enums.GroupStatus;
import com.microlend.exception.ResourceNotFoundException;
import com.microlend.repository.BorrowerGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowerGroupService {

    private final BorrowerGroupRepository groupRepository;

    public BorrowerGroup create(BorrowerGroupRequest req) {
        BorrowerGroup group = BorrowerGroup.builder()
                .groupName(req.getGroupName())
                .centreID(req.getCentreID())
                .fieldOfficerID(req.getFieldOfficerID())
                .formationDate(req.getFormationDate() != null ? req.getFormationDate() : LocalDate.now())
                .memberCount(req.getMemberCount() != null ? req.getMemberCount() : 0)
                .jointLiabilityEnabled(req.getJointLiabilityEnabled() != null ? req.getJointLiabilityEnabled() : false)
                .status(req.getStatus() != null ? req.getStatus() : GroupStatus.ACTIVE)
                .build();
        return groupRepository.save(group);
    }

    public List<BorrowerGroup> getAll() {
        return groupRepository.findAll();
    }

    public BorrowerGroup getById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found: " + id));
    }

    public BorrowerGroup update(Long id, BorrowerGroupRequest req) {
        BorrowerGroup group = getById(id);
        group.setGroupName(req.getGroupName());
        if (req.getMemberCount() != null) group.setMemberCount(req.getMemberCount());
        if (req.getStatus() != null) group.setStatus(req.getStatus());
        if (req.getJointLiabilityEnabled() != null) group.setJointLiabilityEnabled(req.getJointLiabilityEnabled());
        return groupRepository.save(group);
    }

    public List<BorrowerGroup> getByCentre(Long centreID) {
        return groupRepository.findByCentreID(centreID);
    }
}
