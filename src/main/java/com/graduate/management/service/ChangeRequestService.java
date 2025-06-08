package com.graduate.management.service;

import com.graduate.management.dto.ChangeRequestDto;
import com.graduate.management.entity.ChangeRequest;
import com.graduate.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChangeRequestService {
    
    ChangeRequestDto createChangeRequest(ChangeRequestDto changeRequestDto);
    
    ChangeRequestDto updateChangeRequest(Long id, ChangeRequestDto changeRequestDto);
    
    void deleteChangeRequest(Long id);
      ChangeRequestDto getChangeRequestById(Long id, User requester);
    
    List<ChangeRequestDto> getChangeRequestsByStudentProfileId(Long studentProfileId);
    
    Page<ChangeRequestDto> getPendingChangeRequests(Pageable pageable);
    
    ChangeRequest approveChangeRequest(Long id, User reviewer, String comment);
    
    ChangeRequest rejectChangeRequest(Long id, User reviewer, String comment);
    
    // 新增方法
    Page<ChangeRequestDto> getAllChangeRequests(String status, Pageable pageable);
    
    Page<ChangeRequestDto> getCollegeChangeRequests(User user, String status, Pageable pageable);
    
    ChangeRequest cancelChangeRequest(Long id, User user);
    
    ChangeRequestDto convertToDto(ChangeRequest changeRequest);
}
