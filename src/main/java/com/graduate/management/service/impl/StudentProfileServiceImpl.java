package com.graduate.management.service.impl;

import com.graduate.management.dto.StudentProfileDto;
import com.graduate.management.entity.College;
import com.graduate.management.entity.Major;
import com.graduate.management.entity.StudentProfile;
import com.graduate.management.entity.User;
import com.graduate.management.repository.CollegeRepository;
import com.graduate.management.repository.MajorRepository;
import com.graduate.management.repository.StudentProfileRepository;
import com.graduate.management.repository.UserRepository;
import com.graduate.management.service.StudentProfileService;
import com.graduate.management.util.SM4Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final SM4Util sm4Util;

    @Override
    @Transactional
    public StudentProfileDto createStudentProfile(StudentProfileDto dto) {
        User student = userRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        College college = collegeRepository.findById(dto.getCollegeId())
                .orElseThrow(() -> new RuntimeException("学院不存在"));
        
        Major major = majorRepository.findById(dto.getMajorId())
                .orElseThrow(() -> new RuntimeException("专业不存在"));
        
        User supervisor = userRepository.findById(dto.getSupervisorId())
                .orElseThrow(() -> new RuntimeException("导师不存在"));
        
        // 生成学号
        String studentId = generateStudentId(major.getId(), dto.getDegreeType());
        
        StudentProfile studentProfile = new StudentProfile();
        studentProfile.setUser(student);
        studentProfile.setStudentId(studentId);
        studentProfile.setName(dto.getName());
        studentProfile.setGender(dto.getGender());
        studentProfile.setIdNumber(sm4Util.encrypt(dto.getIdNumber())); // 加密身份证号
        studentProfile.setCollege(college);
        studentProfile.setMajor(major);
        studentProfile.setDegreeType(dto.getDegreeType());
        studentProfile.setSupervisor(supervisor);
        studentProfile.setEnrollmentDate(dto.getEnrollmentDate());
        studentProfile.setExpectedGraduationDate(dto.getExpectedGraduationDate());
        
        // 加密敏感信息
        if (dto.getCurrentAddress() != null) {
            studentProfile.setCurrentAddress(sm4Util.encrypt(dto.getCurrentAddress()));
        }
        if (dto.getPermanentAddress() != null) {
            studentProfile.setPermanentAddress(sm4Util.encrypt(dto.getPermanentAddress()));
        }
        studentProfile.setEmergencyContact(dto.getEmergencyContact());
        if (dto.getEmergencyPhone() != null) {
            studentProfile.setEmergencyPhone(sm4Util.encrypt(dto.getEmergencyPhone()));
        }
        
        studentProfile.setEducationBackground(dto.getEducationBackground());
        studentProfile.setWorkExperience(dto.getWorkExperience());
        studentProfile.setPhoto(dto.getPhoto());
        studentProfile.setApproved(false);
        
        StudentProfile saved = studentProfileRepository.save(studentProfile);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public StudentProfileDto updateStudentProfile(Long id, StudentProfileDto dto) {
        StudentProfile studentProfile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        
        // 更新基本信息
        if (dto.getName() != null) {
            studentProfile.setName(dto.getName());
        }
        if (dto.getGender() != null) {
            studentProfile.setGender(dto.getGender());
        }
        if (dto.getIdNumber() != null) {
            studentProfile.setIdNumber(sm4Util.encrypt(dto.getIdNumber()));
        }
        
        // 更新学院和专业信息（如果有变更）
        if (dto.getCollegeId() != null && !dto.getCollegeId().equals(studentProfile.getCollege().getId())) {
            College college = collegeRepository.findById(dto.getCollegeId())
                    .orElseThrow(() -> new RuntimeException("学院不存在"));
            studentProfile.setCollege(college);
        }
        
        if (dto.getMajorId() != null && !dto.getMajorId().equals(studentProfile.getMajor().getId())) {
            Major major = majorRepository.findById(dto.getMajorId())
                    .orElseThrow(() -> new RuntimeException("专业不存在"));
            studentProfile.setMajor(major);
        }
        
        // 更新导师信息
        if (dto.getSupervisorId() != null && !dto.getSupervisorId().equals(studentProfile.getSupervisor().getId())) {
            User supervisor = userRepository.findById(dto.getSupervisorId())
                    .orElseThrow(() -> new RuntimeException("导师不存在"));
            studentProfile.setSupervisor(supervisor);
        }
        
        // 更新其他信息
        if (dto.getDegreeType() != null) {
            studentProfile.setDegreeType(dto.getDegreeType());
        }
        if (dto.getEnrollmentDate() != null) {
            studentProfile.setEnrollmentDate(dto.getEnrollmentDate());
        }
        if (dto.getExpectedGraduationDate() != null) {
            studentProfile.setExpectedGraduationDate(dto.getExpectedGraduationDate());
        }
        
        // 更新并加密敏感信息
        if (dto.getCurrentAddress() != null) {
            studentProfile.setCurrentAddress(sm4Util.encrypt(dto.getCurrentAddress()));
        }
        if (dto.getPermanentAddress() != null) {
            studentProfile.setPermanentAddress(sm4Util.encrypt(dto.getPermanentAddress()));
        }
        if (dto.getEmergencyContact() != null) {
            studentProfile.setEmergencyContact(dto.getEmergencyContact());
        }
        if (dto.getEmergencyPhone() != null) {
            studentProfile.setEmergencyPhone(sm4Util.encrypt(dto.getEmergencyPhone()));
        }
        if (dto.getEducationBackground() != null) {
            studentProfile.setEducationBackground(dto.getEducationBackground());
        }
        if (dto.getWorkExperience() != null) {
            studentProfile.setWorkExperience(dto.getWorkExperience());
        }
        
        // 更新审核状态
        if (dto.getApproved() != null) {
            studentProfile.setApproved(dto.getApproved());
        }
        
        // 如果有审核人
        if (dto.getApproverId() != null) {
            User approver = userRepository.findById(dto.getApproverId())
                    .orElseThrow(() -> new RuntimeException("审核人不存在"));
            studentProfile.setApprover(approver);
            studentProfile.setApproveTime(LocalDateTime.now());
        }
        
        StudentProfile updated = studentProfileRepository.save(studentProfile);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void deleteStudentProfile(Long id) {
        studentProfileRepository.deleteById(id);
    }

    @Override
    public StudentProfileDto getStudentProfileById(Long id) {
        StudentProfile studentProfile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        return convertToDto(studentProfile);
    }

    @Override
    public StudentProfileDto getStudentProfileByStudentId(String studentId) {
        StudentProfile studentProfile = studentProfileRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        return convertToDto(studentProfile);
    }

    @Override
    public StudentProfileDto getStudentProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        StudentProfile studentProfile = studentProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        
        return convertToDto(studentProfile);
    }

    @Override
    public Page<StudentProfileDto> getAllStudentProfiles(Pageable pageable) {
        return studentProfileRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<StudentProfileDto> getStudentProfilesByCollege(Long collegeId, Pageable pageable) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("学院不存在"));
        
        return studentProfileRepository.findByCollege(college, pageable)
                .map(this::convertToDto);
    }

    @Override
    public List<StudentProfileDto> getStudentProfilesBySupervisor(Long supervisorId) {
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new RuntimeException("导师不存在"));
        
        return studentProfileRepository.findBySupervisor(supervisor)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<StudentProfileDto> searchStudentProfiles(String keyword, Pageable pageable) {
        return studentProfileRepository.findByNameContainingOrStudentIdContaining(keyword, keyword, pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<StudentProfileDto> searchStudentProfilesByCollege(Long collegeId, String keyword, Pageable pageable) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("学院不存在"));
        
        return studentProfileRepository.findByCollegeAndNameContainingOrCollegeAndStudentIdContaining(
                college, keyword, college, keyword, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public boolean approveStudentProfile(Long id, Long approverId) {
        StudentProfile studentProfile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new RuntimeException("审核人不存在"));
        
        studentProfile.setApproved(true);
        studentProfile.setApprover(approver);
        studentProfile.setApproveTime(LocalDateTime.now());
        
        studentProfileRepository.save(studentProfile);
        return true;
    }

    @Override
    @Transactional
    public boolean rejectStudentProfile(Long id, String reason) {
        // 在实际应用中，可以添加拒绝的原因和记录
        return false;
    }

    @Override
    public String generateStudentId(Long majorId, String degreeType) {
        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new RuntimeException("专业不存在"));
        
        // 学号规则：年份(4位) + 学院代码(2位) + 专业代码(2位) + 学位类型代码(1位) + 序号(3位)
        // 例如：2023010111001，表示2023年入学，01学院，01专业，硕士(1)，001号
        String year = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String collegeCode = major.getCollege().getCode();
        String majorCode = major.getCode();
        String degreeCode = "硕士".equals(degreeType) ? "1" : "2"; // 1-硕士，2-博士
        
        // 获取当前专业和学位类型下的最大序号
        Integer maxSeq = studentProfileRepository.findMaxSequenceByMajorAndDegreeType(major, degreeType);
        int nextSeq = (maxSeq == null) ? 1 : maxSeq + 1;
        String seqStr = String.format("%03d", nextSeq);
        
        return year + collegeCode + majorCode + degreeCode + seqStr;
    }

    @Override
    @Transactional
    public boolean uploadPhoto(Long id, MultipartFile photo) {
        StudentProfile studentProfile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        
        try {
            studentProfile.setPhoto(photo.getBytes());
            studentProfileRepository.save(studentProfile);
            return true;
        } catch (IOException e) {
            log.error("上传照片失败", e);
            throw new RuntimeException("上传照片失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] getPhoto(Long id) {
        StudentProfile studentProfile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        return studentProfile.getPhoto();
    }

    @Override
    @Transactional
    public boolean importStudentProfiles(MultipartFile file) {
        // 实现批量导入功能
        // 在实际应用中，可以解析Excel或CSV文件，批量导入学生信息
        return false;
    }

    @Override
    public List<StudentProfileDto> getPendingApprovals() {
        return studentProfileRepository.findByApproved(false)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentProfile> findBySupervisor(User supervisor) {
        return studentProfileRepository.findBySupervisor(supervisor);
    }

    @Override
    public StudentProfile findByStudentId(String studentId) {
        return studentProfileRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("学生不存在"));
    }

    // 将实体转换为DTO
    private StudentProfileDto convertToDto(StudentProfile studentProfile) {
        StudentProfileDto dto = new StudentProfileDto();
        dto.setId(studentProfile.getId());
        dto.setStudentId(studentProfile.getStudentId());
        dto.setName(studentProfile.getName());
        dto.setGender(studentProfile.getGender());
        
        // 解密敏感信息
        if (studentProfile.getIdNumber() != null) {
            String idNumber = sm4Util.decrypt(studentProfile.getIdNumber());
            dto.setIdNumber(idNumber);
        }
        
        dto.setCollegeId(studentProfile.getCollege().getId());
        dto.setCollegeName(studentProfile.getCollege().getName());
        dto.setMajorId(studentProfile.getMajor().getId());
        dto.setMajorName(studentProfile.getMajor().getName());
        dto.setDegreeType(studentProfile.getDegreeType());
        dto.setSupervisorId(studentProfile.getSupervisor().getId());
        dto.setSupervisorName(studentProfile.getSupervisor().getName());
        dto.setEnrollmentDate(studentProfile.getEnrollmentDate());
        dto.setExpectedGraduationDate(studentProfile.getExpectedGraduationDate());
        
        // 解密敏感地址信息
        if (studentProfile.getCurrentAddress() != null) {
            dto.setCurrentAddress(sm4Util.decrypt(studentProfile.getCurrentAddress()));
        }
        if (studentProfile.getPermanentAddress() != null) {
            dto.setPermanentAddress(sm4Util.decrypt(studentProfile.getPermanentAddress()));
        }
        
        dto.setEmergencyContact(studentProfile.getEmergencyContact());
        
        // 解密敏感电话信息
        if (studentProfile.getEmergencyPhone() != null) {
            dto.setEmergencyPhone(sm4Util.decrypt(studentProfile.getEmergencyPhone()));
        }
        
        dto.setEducationBackground(studentProfile.getEducationBackground());
        dto.setWorkExperience(studentProfile.getWorkExperience());
        dto.setPhoto(studentProfile.getPhoto());
        dto.setApproved(studentProfile.getApproved());
        
        if (studentProfile.getApprover() != null) {
            dto.setApproverId(studentProfile.getApprover().getId());
            dto.setApproverName(studentProfile.getApprover().getName());
        }
        
        return dto;
    }
}
