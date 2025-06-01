package com.graduate.management.service;

import com.graduate.management.dto.ChangeRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChangeRequestService {
    
    ChangeRequestDto createChangeRequest(ChangeRequestDto changeRequestDto);
    
    ChangeRequestDto updateChangeRequest(Long id, ChangeRequestDto changeRequestDto);
    
    void deleteChangeRequest(Long id);
    
    ChangeRequestDto getChangeRequestById(Long id);
    
    List<ChangeRequestDto> getChangeRequestsByStudentProfileId(Long studentProfileId);
    
    Page<ChangeRequestDto> getPendingChangeRequests(Pageable pageable);
    
    boolean approveChangeRequest(Long id, Long reviewerId, String comment);
    
    boolean rejectChangeRequest(Long id, Long reviewerId, String comment);
}
