package com.graduate.management.service;

import com.graduate.management.dto.StudentProfileDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StudentProfileService {
    
    StudentProfileDto createStudentProfile(StudentProfileDto studentProfileDto);
    
    StudentProfileDto updateStudentProfile(Long id, StudentProfileDto studentProfileDto);
    
    void deleteStudentProfile(Long id);
    
    StudentProfileDto getStudentProfileById(Long id);
    
    StudentProfileDto getStudentProfileByStudentId(String studentId);
    
    StudentProfileDto getStudentProfileByUsername(String username);
    
    Page<StudentProfileDto> getAllStudentProfiles(Pageable pageable);
    
    Page<StudentProfileDto> getStudentProfilesByCollege(Long collegeId, Pageable pageable);
    
    List<StudentProfileDto> getStudentProfilesBySupervisor(Long supervisorId);
    
    Page<StudentProfileDto> searchStudentProfiles(String keyword, Pageable pageable);
    
    Page<StudentProfileDto> searchStudentProfilesByCollege(Long collegeId, String keyword, Pageable pageable);
    
    boolean approveStudentProfile(Long id, Long approverId);
    
    boolean rejectStudentProfile(Long id, String reason);
    
    String generateStudentId(Long majorId, String degreeType);
    
    boolean uploadPhoto(Long id, MultipartFile photo);
    
    byte[] getPhoto(Long id);
    
    boolean importStudentProfiles(MultipartFile file);
    
    List<StudentProfileDto> getPendingApprovals();
    
    com.graduate.management.entity.StudentProfile findByStudentId(String studentId);
    
    List<com.graduate.management.entity.StudentProfile> findBySupervisor(com.graduate.management.entity.User supervisor);
    
    StudentProfileDto convertToDto(com.graduate.management.entity.StudentProfile studentProfile);
}
