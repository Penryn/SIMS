package com.graduate.management.service;

import com.graduate.management.dto.StudentProfileDto;
import com.graduate.management.entity.StudentProfile;
import com.graduate.management.entity.User;
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
    
    StudentProfile findByStudentId(String studentId);
    
    List<StudentProfile> findBySupervisor(User supervisor);
    
    StudentProfileDto convertToDto(StudentProfile studentProfile);
    
    // 批量创建学生学籍信息方法
    List<StudentProfile> batchCreateStudentProfiles(List<StudentProfile> studentProfiles);
    
    // 新增方法：根据ID查找学生学籍信息
    StudentProfile findById(Long id);
    
    // 新增方法：根据学生用户查找学籍信息
    StudentProfile findByStudent(User student);
    
    // 新增方法：学生更新自己的学籍信息（限制字段）
    StudentProfile updateStudentProfileByStudent(StudentProfile existingProfile, StudentProfile updatedProfile);
    
    // 新增方法：学院人员更新学生信息（需要审核）
    StudentProfile updateStudentProfileWithApproval(StudentProfile profile, User updater);
    
    // 新增方法：直接更新学生信息（无需审核）
    StudentProfile updateStudentProfile(StudentProfile profile);
    
    // 新增方法：获取学生信息（基于角色和筛选条件）
    Page<StudentProfileDto> getStudentProfiles(User currentUser, Long collegeId, Long majorId, String keyword, Pageable pageable);
    
    // 新增方法：检查当前用户是否与学生属于同一学院
    boolean isFromSameCollege(Long studentProfileId, User user);
}
