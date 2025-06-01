package com.graduate.management.repository;

import com.graduate.management.entity.ChangeRequest;
import com.graduate.management.entity.StudentProfile;
import com.graduate.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequest, Long> {
    
    List<ChangeRequest> findByStudentProfile(StudentProfile studentProfile);
    
    List<ChangeRequest> findByRequester(User requester);
    
    List<ChangeRequest> findByReviewer(User reviewer);
    
    List<ChangeRequest> findByStatus(String status);
    
    Page<ChangeRequest> findByStatus(String status, Pageable pageable);
    
    Page<ChangeRequest> findByStudentProfileAndStatus(StudentProfile studentProfile, String status, Pageable pageable);
}
