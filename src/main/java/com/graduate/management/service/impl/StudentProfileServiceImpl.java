package com.graduate.management.service.impl;

import com.graduate.management.dto.StudentProfileDto;
import com.graduate.management.entity.College;
import com.graduate.management.entity.Major;
import com.graduate.management.entity.Role;
import com.graduate.management.entity.StudentProfile;
import com.graduate.management.entity.User;
import com.graduate.management.repository.CollegeRepository;
import com.graduate.management.repository.MajorRepository;
import com.graduate.management.repository.RoleRepository;
import com.graduate.management.repository.StudentProfileRepository;
import com.graduate.management.repository.UserRepository;
import com.graduate.management.security.SM3PasswordEncoder;
import com.graduate.management.security.UserDetailsImpl;
import com.graduate.management.service.StudentProfileService;
import com.graduate.management.util.DtoMaskUtil;
import com.graduate.management.util.SM4Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final MajorRepository majorRepository;
    private final RoleRepository roleRepository;
    private final DtoMaskUtil dtoMaskUtil;
    private final SM4Util sm4Util;
    private final SM3PasswordEncoder sm3PasswordEncoder;

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
    }    @Override
    @Transactional
    public boolean rejectStudentProfile(Long id, String reason) {
        log.info("拒绝学生学籍信息审核，ID: {}, 原因: {}", id, reason);
        
        StudentProfile studentProfile = studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        
        // 如果已经通过审核，不能再拒绝
        if (studentProfile.getApproved()) {
            throw new IllegalStateException("该学生学籍信息已通过审核，无法拒绝");
        }
        
        // 设置拒绝原因（可以在StudentProfile实体中添加rejectReason字段）
        // 这里模拟发送通知给学院研究生秘书或提交者
        String message = "学生 " + studentProfile.getName() + " (学号: " + studentProfile.getStudentId() 
                + ") 的学籍信息未通过审核，原因: " + reason;
        log.info(message);
        
        // 如果系统集成了消息通知功能，可以在这里发送通知
        // messageService.sendNotification(studentProfile.getUser().getId(), message);
        
        return true;
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
    }    @Override
    @Transactional
    public boolean importStudentProfiles(MultipartFile file) {
        log.info("开始从文件导入学生学籍信息: {}", file.getOriginalFilename());
        try {        // 检查文件是否为Excel
        String fileName = file.getOriginalFilename();
        if (!isExcelFile(fileName)) {
                log.error("文件格式不正确，仅支持Excel文件(.xlsx, .xls)");
                throw new IllegalArgumentException("文件格式不正确，仅支持Excel文件(.xlsx, .xls)");
            }
            
            // 使用Apache POI解析Excel文件
            List<StudentProfile> studentProfiles = parseExcelFile(file);
            
            if (studentProfiles.isEmpty()) {
                log.warn("Excel文件中没有有效的学生信息");
                return false;
            }
            
            // 批量创建学生学籍信息
            List<StudentProfile> createdProfiles = batchCreateStudentProfiles(studentProfiles);
            
            log.info("学生学籍信息导入成功，共导入{}条记录", createdProfiles.size());
            return !createdProfiles.isEmpty();
            
        } catch (Exception e) {
            log.error("导入学生学籍信息失败: {}", e.getMessage(), e);
            throw new RuntimeException("导入学生学籍信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 检查文件是否为Excel文件
     *
     * @param fileName 文件名
     * @return 是否为Excel文件
     */
    private boolean isExcelFile(String fileName) {
        return fileName != null && 
               (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"));
    }
      /**
     * 解析Excel文件，提取学生信息
     *
     * @param file Excel文件
     * @return 学生学籍信息列表
     */
    private List<StudentProfile> parseExcelFile(MultipartFile file) {
        log.info("开始解析Excel文件: {}", file.getOriginalFilename());
        List<StudentProfile> studentProfiles = new ArrayList<>();
          try (InputStream is = file.getInputStream()) {
            Workbook workbook = null;
            
            try {
                // 根据文件扩展名确定Workbook类型
                String fileName = file.getOriginalFilename();
                if (fileName != null && fileName.endsWith(".xlsx")) {
                    workbook = new XSSFWorkbook(is); // Excel 2007+
                } else {
                    workbook = new HSSFWorkbook(is); // Excel 97-2003
                }
                
                // 获取第一个工作表
                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null) {
                    throw new IllegalArgumentException("Excel文件中不包含工作表");
                }
                
                // 获取表头行
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    throw new IllegalArgumentException("Excel文件中没有表头");
                }
                
                // 检查必要的列是否存在
                Map<String, Integer> columnMap = validateAndMapColumns(headerRow);
                
                // 从第二行开始解析数据(第一行是表头)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    try {
                        StudentProfile profile = createProfileFromRow(row, columnMap);
                        if (profile != null) {
                            studentProfiles.add(profile);
                        }
                    } catch (Exception e) {
                        log.warn("解析第{}行数据失败: {}", i + 1, e.getMessage());
                    }
                }
                
                log.info("成功解析{}条学生信息", studentProfiles.size());
                
            } finally {
                if (workbook != null) {
                    workbook.close();
                }
            }
            
        } catch (Exception e) {
            log.error("解析Excel文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法解析Excel文件: " + e.getMessage(), e);
        }
        
        return studentProfiles;
    }
    
    /**
     * 验证表头并创建列映射
     *
     * @param headerRow 表头行
     * @return 列名到列索引的映射
     */
    private Map<String, Integer> validateAndMapColumns(Row headerRow) {
        Map<String, Integer> columnMap = new HashMap<>();
        
        // 必需的列名，这些是Excel模板中的列标题
        List<String> requiredColumns = Arrays.asList("姓名", "性别", "身份证号", "学院", "专业", "学位类型", "导师");
        
        // 映射列名和索引
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell != null && cell.getCellType() == CellType.STRING) {
                String columnName = cell.getStringCellValue().trim();
                columnMap.put(columnName, i);
            }
        }
        
        // 检查必需的列是否存在
        List<String> missingColumns = new ArrayList<>();
        for (String requiredColumn : requiredColumns) {
            if (!columnMap.containsKey(requiredColumn)) {
                missingColumns.add(requiredColumn);
            }
        }
        
        if (!missingColumns.isEmpty()) {
            throw new IllegalArgumentException("Excel模板缺少必需的列: " + String.join(", ", missingColumns));
        }
        
        return columnMap;
    }
    
    /**
     * 从Excel行创建学生档案对象
     *
     * @param row 数据行
     * @param columnMap 列名到列索引的映射
     * @return 学生档案对象
     */
    private StudentProfile createProfileFromRow(Row row, Map<String, Integer> columnMap) {
        // 获取必要的数据
        String name = getCellValueAsString(row, columnMap.get("姓名"));
        String gender = getCellValueAsString(row, columnMap.get("性别"));
        String idNumber = getCellValueAsString(row, columnMap.get("身份证号"));
        String collegeName = getCellValueAsString(row, columnMap.get("学院"));
        String majorName = getCellValueAsString(row, columnMap.get("专业"));
        String degreeType = getCellValueAsString(row, columnMap.get("学位类型"));
        String supervisorName = getCellValueAsString(row, columnMap.get("导师"));
        
        // 基本数据验证
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(gender) || StringUtils.isEmpty(idNumber)) {
            log.warn("行数据缺少必要信息: 姓名={}, 性别={}, 身份证号={}", name, gender, idNumber);
            return null;
        }
        
        // 查找或创建对应实体
        StudentProfile profile = new StudentProfile();
        profile.setName(name);
        profile.setGender(gender);
        profile.setIdNumber(idNumber); // 数据加密将在batchCreateStudentProfiles方法中处理
        
        // 设置学院
        try {
            College college = findCollegeByName(collegeName);
            profile.setCollege(college);
        } catch (Exception e) {
            log.error("找不到学院: {}", collegeName);
            return null;
        }
        
        // 设置专业
        try {
            Major major = findMajorByNameAndCollege(majorName, profile.getCollege());
            profile.setMajor(major);
        } catch (Exception e) {
            log.error("找不到专业: {} (学院: {})", majorName, collegeName);
            return null;
        }
        
        // 设置学位类型
        profile.setDegreeType(degreeType);
        
        // 设置导师
        try {
            User supervisor = findSupervisorByName(supervisorName);
            profile.setSupervisor(supervisor);
        } catch (Exception e) {
            log.error("找不到导师: {}", supervisorName);
            return null;
        }
        
        // 设置可选字段
        if (columnMap.containsKey("入学时间")) {
            String enrollmentDateStr = getCellValueAsString(row, columnMap.get("入学时间"));
            if (!StringUtils.isEmpty(enrollmentDateStr)) {
                try {
                    LocalDate enrollmentDate = parseDate(enrollmentDateStr);
                    profile.setEnrollmentDate(enrollmentDate);
                } catch (Exception e) {
                    log.warn("无法解析入学时间: {}", enrollmentDateStr);
                }
            }
        }
        
        if (columnMap.containsKey("预计毕业时间")) {
            String graduationDateStr = getCellValueAsString(row, columnMap.get("预计毕业时间"));
            if (!StringUtils.isEmpty(graduationDateStr)) {
                try {
                    LocalDate graduationDate = parseDate(graduationDateStr);
                    profile.setExpectedGraduationDate(graduationDate);
                } catch (Exception e) {
                    log.warn("无法解析预计毕业时间: {}", graduationDateStr);
                }
            }
        }
        
        // 设置其他可选字段
        if (columnMap.containsKey("现居地址")) {
            profile.setCurrentAddress(getCellValueAsString(row, columnMap.get("现居地址")));
        }
        if (columnMap.containsKey("永久地址")) {
            profile.setPermanentAddress(getCellValueAsString(row, columnMap.get("永久地址")));
        }
        if (columnMap.containsKey("紧急联系人")) {
            profile.setEmergencyContact(getCellValueAsString(row, columnMap.get("紧急联系人")));
        }
        if (columnMap.containsKey("紧急联系人电话")) {
            profile.setEmergencyPhone(getCellValueAsString(row, columnMap.get("紧急联系人电话")));
        }
        if (columnMap.containsKey("教育背景")) {
            profile.setEducationBackground(getCellValueAsString(row, columnMap.get("教育背景")));
        }
        if (columnMap.containsKey("工作经历")) {
            profile.setWorkExperience(getCellValueAsString(row, columnMap.get("工作经历")));
        }
        
        // 设置默认的审核状态
        profile.setApproved(false);
        
        // 设置创建者
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User creator = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("无法获取当前用户信息"));
            profile.setCreatedBy(creator);
        }
        
        return profile;
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
    }    // 将实体转换为DTO    @Override
    public StudentProfileDto convertToDto(StudentProfile studentProfile) {
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
          // 检查是否是学生本人查看，如果不是则进行脱敏处理
        boolean isSelfView = checkIfSelfView(studentProfile.getStudentId());
        return dtoMaskUtil.maskStudentProfile(dto, isSelfView);
    }

    @Override
    @Transactional
    public List<StudentProfile> batchCreateStudentProfiles(List<StudentProfile> studentProfiles) {
        log.info("开始批量创建学生学籍信息，共{}条记录", studentProfiles.size());
        List<StudentProfile> createdProfiles = new ArrayList<>();
        
        for (StudentProfile profile : studentProfiles) {
            try {
                // 验证必要字段
                validateStudentProfile(profile);
                
                // 生成学号
                if (profile.getStudentId() == null || profile.getStudentId().isEmpty()) {
                    String studentId = generateStudentId(profile.getMajor().getId(), profile.getDegreeType());
                    profile.setStudentId(studentId);
                }
                
                // 加密敏感信息
                if (profile.getIdNumber() != null) {
                    profile.setIdNumber(sm4Util.encrypt(profile.getIdNumber()));
                }
                if (profile.getCurrentAddress() != null) {
                    profile.setCurrentAddress(sm4Util.encrypt(profile.getCurrentAddress()));
                }
                if (profile.getPermanentAddress() != null) {
                    profile.setPermanentAddress(sm4Util.encrypt(profile.getPermanentAddress()));
                }
                if (profile.getEmergencyPhone() != null) {
                    profile.setEmergencyPhone(sm4Util.encrypt(profile.getEmergencyPhone()));
                }
                
                // 设置默认状态
                profile.setApproved(false);
                
                // 创建对应的用户账号（如果尚未存在）
                if (profile.getUser() == null) {
                    User user = createUserForStudent(profile);
                    profile.setUser(user);
                }
                
                // 保存到数据库
                StudentProfile savedProfile = studentProfileRepository.save(profile);
                createdProfiles.add(savedProfile);
                log.info("成功创建学生学籍信息：{}", savedProfile.getStudentId());
                
            } catch (Exception e) {
                log.error("创建学生学籍信息失败: {}", e.getMessage(), e);
                // 继续处理下一条记录
            }
        }
        
        return createdProfiles;
    }
    
    /**
     * 验证学生学籍信息的必要字段
     *
     * @param profile 学生学籍信息
     */
    private void validateStudentProfile(StudentProfile profile) {
        if (profile.getName() == null || profile.getName().isEmpty()) {
            throw new IllegalArgumentException("学生姓名不能为空");
        }
        if (profile.getGender() == null || profile.getGender().isEmpty()) {
            throw new IllegalArgumentException("学生性别不能为空");
        }
        if (profile.getIdNumber() == null || profile.getIdNumber().isEmpty()) {
            throw new IllegalArgumentException("身份证号不能为空");
        }
        if (profile.getCollege() == null || profile.getCollege().getId() == null) {
            throw new IllegalArgumentException("学院信息不能为空");
        }
        if (profile.getMajor() == null || profile.getMajor().getId() == null) {
            throw new IllegalArgumentException("专业信息不能为空");
        }
        if (profile.getDegreeType() == null || profile.getDegreeType().isEmpty()) {
            throw new IllegalArgumentException("学位类型不能为空");
        }
        if (profile.getSupervisor() == null || profile.getSupervisor().getId() == null) {
            throw new IllegalArgumentException("导师信息不能为空");
        }
    }
    
    /**
     * 为学生创建对应的用户账号
     *
     * @param profile 学生学籍信息
     * @return 创建的用户账号
     */
    private User createUserForStudent(StudentProfile profile) {
        User user = new User();
        user.setUsername(profile.getStudentId());
        user.setName(profile.getName());
          // 设置默认密码（身份证号后8位）
        String idNumber = profile.getIdNumber();
        if (idNumber.length() >= 8) {
            String defaultPassword = idNumber.substring(idNumber.length() - 8);
            user.setPassword(sm3PasswordEncoder.encode(defaultPassword));
        } else {
            throw new IllegalArgumentException("身份证号格式不正确，无法设置默认密码");
        }
        
        // 设置用户角色为STUDENT
        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new RuntimeException("学生角色不存在"));
        Set<Role> roles = new HashSet<>();
        roles.add(studentRole);
        user.setRoles(roles);
        
        // 设置其他属性
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setFirstLogin(true);
        user.setLastPasswordChangeTime(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * 检查当前登录用户是否是查看自己的学籍信息
     *
     * @param studentId 学生ID
     * @return 是否是本人查看
     */
    private boolean checkIfSelfView(String studentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        Object principal = authentication.getPrincipal();        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            // 如果当前登录用户是学生，且查看的是自己的学籍信息
            return userDetails.getUsername().equals(studentId);
        }
        
        return false;
    }

    @Override
    public StudentProfile findById(Long id) {
        return studentProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在，ID: " + id));
    }

    @Override
    public StudentProfile findByStudent(User student) {
        return studentProfileRepository.findByUser(student)
                .orElseThrow(() -> new RuntimeException("未找到该学生的学籍信息"));
    }    @Override
    public boolean isFromSameCollege(Long studentProfileId, User user) {
        // 获取学生所属学院
        StudentProfile studentProfile = findById(studentProfileId);
        College studentCollege = studentProfile.getCollege();
        
        // 在实际应用中，我们需要通过一些方式确定用户所属的学院
        // 可能需要添加额外的表或关系来关联用户和学院
        
        // 以下是一种可能的实现方式：
        // 1. 如果用户是学院管理员，检查其角色中是否有针对特定学院的权限标记
        boolean isCollegeAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_COLLEGE_ADMIN") || 
                                 role.getName().equals("ROLE_COLLEGE_SECRETARY"));
        
        if (isCollegeAdmin) {
            // 这里简化处理，假设管理员只能管理一个学院
            // 在实际中，可能需要额外的用户-学院关系表
            
            // 检查该用户管理的学生中是否都来自于同一个学院
            List<StudentProfile> managedProfiles = studentProfileRepository.findByCreatedBy(user);
            if (!managedProfiles.isEmpty()) {
                College userCollege = managedProfiles.get(0).getCollege();
                return userCollege != null && userCollege.getId().equals(studentCollege.getId());
            }
        }
        
        // 如果无法确定用户所属学院，则默认返回false
        return false;
    }

    @Override
    @Transactional
    public StudentProfile updateStudentProfile(StudentProfile profile) {
        // 获取当前的学生信息
        StudentProfile existingProfile = findById(profile.getId());
        
        // 研究生院管理员可以直接修改所有信息，包括敏感信息
        
        // 更新基本信息
        if (profile.getName() != null) {
            existingProfile.setName(profile.getName());
        }
        if (profile.getGender() != null) {
            existingProfile.setGender(profile.getGender());
        }
        if (profile.getIdNumber() != null) {
            existingProfile.setIdNumber(sm4Util.encrypt(profile.getIdNumber()));
        }
        
        // 更新学院、专业、学位类型和导师信息
        if (profile.getCollege() != null) {
            existingProfile.setCollege(profile.getCollege());
            
            // 如果学院变了，学号可能需要变更
            if (profile.getMajor() != null && profile.getDegreeType() != null) {
                String newStudentId = generateStudentId(profile.getMajor().getId(), profile.getDegreeType());
                existingProfile.setStudentId(newStudentId);
                
                // 同时也要更新对应用户的用户名
                User user = existingProfile.getUser();
                user.setUsername(newStudentId);
                userRepository.save(user);
            }
        }
        
        if (profile.getMajor() != null) {
            existingProfile.setMajor(profile.getMajor());
        }
        
        if (profile.getDegreeType() != null) {
            existingProfile.setDegreeType(profile.getDegreeType());
        }
        
        if (profile.getSupervisor() != null) {
            existingProfile.setSupervisor(profile.getSupervisor());
        }
        
        // 更新其他信息
        if (profile.getEnrollmentDate() != null) {
            existingProfile.setEnrollmentDate(profile.getEnrollmentDate());
        }
        
        if (profile.getExpectedGraduationDate() != null) {
            existingProfile.setExpectedGraduationDate(profile.getExpectedGraduationDate());
        }
        
        // 更新加密存储的敏感信息
        if (profile.getCurrentAddress() != null) {
            existingProfile.setCurrentAddress(sm4Util.encrypt(profile.getCurrentAddress()));
        }
        
        if (profile.getPermanentAddress() != null) {
            existingProfile.setPermanentAddress(sm4Util.encrypt(profile.getPermanentAddress()));
        }
        
        if (profile.getEmergencyContact() != null) {
            existingProfile.setEmergencyContact(profile.getEmergencyContact());
        }
        
        if (profile.getEmergencyPhone() != null) {
            existingProfile.setEmergencyPhone(sm4Util.encrypt(profile.getEmergencyPhone()));
        }
        
        // 更新教育和工作背景
        if (profile.getEducationBackground() != null) {
            existingProfile.setEducationBackground(profile.getEducationBackground());
        }
        
        if (profile.getWorkExperience() != null) {
            existingProfile.setWorkExperience(profile.getWorkExperience());
        }
        
        // 更新审核状态
        existingProfile.setApproved(true);
        
        return studentProfileRepository.save(existingProfile);
    }
    
    @Override
    @Transactional
    public StudentProfile updateStudentProfileByStudent(StudentProfile existingProfile, StudentProfile updatedProfile) {
        // 学生只能更新部分个人信息，不能修改基本信息如姓名、学号、学院、专业等
        
        // 更新联系信息（加密存储）
        if (updatedProfile.getCurrentAddress() != null) {
            existingProfile.setCurrentAddress(sm4Util.encrypt(updatedProfile.getCurrentAddress()));
        }
        if (updatedProfile.getPermanentAddress() != null) {
            existingProfile.setPermanentAddress(sm4Util.encrypt(updatedProfile.getPermanentAddress()));
        }
        if (updatedProfile.getEmergencyContact() != null) {
            existingProfile.setEmergencyContact(updatedProfile.getEmergencyContact());
        }
        if (updatedProfile.getEmergencyPhone() != null) {
            existingProfile.setEmergencyPhone(sm4Util.encrypt(updatedProfile.getEmergencyPhone()));
        }
        
        // 更新教育和工作背景
        if (updatedProfile.getEducationBackground() != null) {
            existingProfile.setEducationBackground(updatedProfile.getEducationBackground());
        }
        if (updatedProfile.getWorkExperience() != null) {
            existingProfile.setWorkExperience(updatedProfile.getWorkExperience());
        }
        
        // 保存更新
        return studentProfileRepository.save(existingProfile);
    }

    @Override
    @Transactional
    public StudentProfile updateStudentProfileWithApproval(StudentProfile profile, User updater) {
        // 获取当前的学生信息
        StudentProfile existingProfile = findById(profile.getId());
        
        // 创建变更请求记录
        // 这里假设系统有ChangeRequestService来处理变更请求
        // 在实际应用中，需要创建变更请求记录，并等待研究生院管理员审核
        
        // 可以更新的基本信息
        if (profile.getName() != null && !profile.getName().equals(existingProfile.getName())) {
            createChangeRequest(existingProfile, "name", existingProfile.getName(), profile.getName(), updater);
        }
        if (profile.getGender() != null && !profile.getGender().equals(existingProfile.getGender())) {
            createChangeRequest(existingProfile, "gender", existingProfile.getGender(), profile.getGender(), updater);
        }
        if (profile.getIdNumber() != null) {
            String decryptedExisting = sm4Util.decrypt(existingProfile.getIdNumber());
            if (!profile.getIdNumber().equals(decryptedExisting)) {
                createChangeRequest(existingProfile, "idNumber", decryptedExisting, profile.getIdNumber(), updater);
            }
        }
        
        // 学院、专业、学位类型和导师变更
        if (profile.getCollege() != null && !profile.getCollege().getId().equals(existingProfile.getCollege().getId())) {
            createChangeRequest(existingProfile, "college", 
                    existingProfile.getCollege().getName(), profile.getCollege().getName(), updater);
            
            // 如果学院变了，学号可能需要变更
            if (profile.getMajor() != null && profile.getDegreeType() != null) {
                String newStudentId = generateStudentId(profile.getMajor().getId(), profile.getDegreeType());
                createChangeRequest(existingProfile, "studentId", existingProfile.getStudentId(), newStudentId, updater);
            }
        }
        
        if (profile.getMajor() != null && !profile.getMajor().getId().equals(existingProfile.getMajor().getId())) {
            createChangeRequest(existingProfile, "major", 
                    existingProfile.getMajor().getName(), profile.getMajor().getName(), updater);
        }
        
        if (profile.getDegreeType() != null && !profile.getDegreeType().equals(existingProfile.getDegreeType())) {
            createChangeRequest(existingProfile, "degreeType", 
                    existingProfile.getDegreeType(), profile.getDegreeType(), updater);
        }
        
        if (profile.getSupervisor() != null && !profile.getSupervisor().getId().equals(existingProfile.getSupervisor().getId())) {
            createChangeRequest(existingProfile, "supervisor", 
                    existingProfile.getSupervisor().getName(), profile.getSupervisor().getName(), updater);
        }
        
        // 其他非敏感信息可以直接更新，不需要审核
        updateNonSensitiveInfo(existingProfile, profile);
        
        // 返回更新后的对象
        return studentProfileRepository.save(existingProfile);
    }
    
    /**
     * 创建信息变更申请
     *
     * @param studentProfile 学生学籍信息
     * @param fieldName 变更的字段名称
     * @param oldValue 原值
     * @param newValue 新值
     * @param requester 申请人
     */
    private void createChangeRequest(StudentProfile studentProfile, String fieldName, String oldValue, String newValue, User requester) {
        // 在实际应用中，这里需要调用ChangeRequestService创建变更申请
        log.info("创建变更申请：学生[{}]，字段[{}]，旧值[{}]，新值[{}]，申请人[{}]",
                studentProfile.getStudentId(), fieldName, oldValue, newValue, requester.getUsername());
    }
    
    /**
     * 更新非敏感信息（不需要审核的信息）
     *
     * @param existingProfile 现有学籍信息
     * @param updatedProfile 更新的学籍信息
     */
    private void updateNonSensitiveInfo(StudentProfile existingProfile, StudentProfile updatedProfile) {
        // 更新入学、毕业日期等非敏感信息
        if (updatedProfile.getEnrollmentDate() != null) {
            existingProfile.setEnrollmentDate(updatedProfile.getEnrollmentDate());
        }
        if (updatedProfile.getExpectedGraduationDate() != null) {
            existingProfile.setExpectedGraduationDate(updatedProfile.getExpectedGraduationDate());
        }
        
        // 更新教育和工作背景
        if (updatedProfile.getEducationBackground() != null) {
            existingProfile.setEducationBackground(updatedProfile.getEducationBackground());
        }
        if (updatedProfile.getWorkExperience() != null) {
            existingProfile.setWorkExperience(updatedProfile.getWorkExperience());
        }
    }

    @Override
    public Page<StudentProfileDto> getStudentProfiles(User currentUser, Long collegeId, Long majorId, String keyword, Pageable pageable) {
        log.info("获取学生学籍信息列表 - 用户: {}, 学院ID: {}, 专业ID: {}, 关键词: {}", 
                currentUser.getUsername(), collegeId, majorId, keyword);
        Page<StudentProfile> profilesPage;
        
        // 根据用户角色确定查询范围
        if (hasRole(currentUser, "ROLE_ADMIN") || hasRole(currentUser, "ROLE_GRADUATE_ADMIN")) {
            // 管理员可以查看全部学生，但可以按学院、专业和关键词筛选
            log.debug("用户拥有管理员权限，可查看所有学生信息");
            profilesPage = queryStudentProfiles(collegeId, majorId, keyword, pageable);
        } else if (hasRole(currentUser, "ROLE_COLLEGE_ADMIN") || hasRole(currentUser, "ROLE_COLLEGE_SECRETARY")) {
            // 学院管理员只能查看本学院学生
            // 通过用户关联的学院确定其所属学院
            log.debug("用户拥有学院管理员权限，查询其所属学院的学生信息");
            
            College userCollege = getUserCollege(currentUser);
            if (userCollege == null) {
                log.warn("无法确定用户所属学院，返回空结果");
                return Page.empty(pageable);
            }
            
            // 强制使用用户所属学院ID作为筛选条件
            collegeId = userCollege.getId();
            log.debug("限制用户只能查看学院ID: {}的学生信息", collegeId);
            profilesPage = queryStudentProfiles(collegeId, majorId, keyword, pageable);
        } else if (hasRole(currentUser, "ROLE_TEACHER")) {
            // 导师只能查看其指导的学生
            log.debug("用户是导师，只能查看其指导的学生");
            
            // 如果提供了专业筛选条件
            if (majorId != null) {
                Major major = majorRepository.findById(majorId)
                        .orElseThrow(() -> new RuntimeException("专业不存在"));
                
                if (keyword != null && !keyword.isEmpty()) {
                    // 按导师、专业和关键词筛选
                    profilesPage = studentProfileRepository.findBySupervisorAndMajorAndNameOrStudentIdContaining(
                            currentUser, major, keyword, keyword, pageable);
                } else {
                    // 只按导师和专业筛选
                    profilesPage = studentProfileRepository.findBySupervisorAndMajor(currentUser, major, pageable);
                }
            } else {
                // 未指定专业，仅按导师和可能的关键词筛选
                if (keyword != null && !keyword.isEmpty()) {
                    profilesPage = studentProfileRepository.findBySupervisorAndNameContainingOrStudentIdContaining(
                            currentUser, keyword, keyword, pageable);
                } else {
                    profilesPage = studentProfileRepository.findBySupervisor(currentUser, pageable);
                }
            }
        } else {
            // 学生用户可能只能查看自己的信息，这部分在controller层通过另一个接口处理
            // 其他角色没有权限查看学生列表
            log.warn("用户无权查看学生列表，角色不满足要求");
            return Page.empty(pageable);
        }
        
        log.info("查询结果: 总记录数: {}, 总页数: {}", profilesPage.getTotalElements(), profilesPage.getTotalPages());
        
        // 转换为DTO并返回，应用适当的脱敏处理
        return profilesPage.map(profile -> {
            StudentProfileDto dto = convertToDto(profile);
            // 检查是否是查看自己的信息，对管理员、学院管理员和导师则不进行脱敏
            boolean shouldMask = !isAdminOrTeacherOfStudent(currentUser, profile);
            return shouldMask ? dtoMaskUtil.maskStudentProfile(dto, false) : dto;
        });
    }
    
    /**
     * 判断用户是否是该学生的管理员或导师
     * 
     * @param user 当前用户
     * @param studentProfile 学生档案
     * @return 是否是管理员或导师
     */
    private boolean isAdminOrTeacherOfStudent(User user, StudentProfile studentProfile) {
        // 如果是系统管理员或研究生管理员，可以查看完整信息
        if (hasRole(user, "ROLE_ADMIN") || hasRole(user, "ROLE_GRADUATE_ADMIN")) {
            return true;
        }
        
        // 如果是学院管理员，只有管理相同学院的学生信息时才能查看完整信息
        if (hasRole(user, "ROLE_COLLEGE_ADMIN") || hasRole(user, "ROLE_COLLEGE_SECRETARY")) {
            College userCollege = getUserCollege(user);
            return userCollege != null && userCollege.getId().equals(studentProfile.getCollege().getId());
        }
        
        // 如果是导师，只有是该学生的导师时才能查看完整信息
        if (hasRole(user, "ROLE_TEACHER")) {
            return user.getId().equals(studentProfile.getSupervisor().getId());
        }
        
        // 其他情况返回false
        return false;
    }
    
    /**
     * 获取用户所属学院
     *
     * @param user 用户
     * @return 用户所属学院，如果无法确定则返回null
     */
    private College getUserCollege(User user) {
        // 这里需要根据实际业务逻辑来实现获取用户所属学院的方法
        // 例如，可以通过用户扩展信息、用户创建的学生记录、或专门的用户-学院关联表来获取
        
        // 以下为示例实现，实际应根据系统设计调整
        // 方法1：假设系统中有UserCollege关联表或UserDetail表记录用户所属学院
        // return userCollegeRepository.findCollegeByUser(user);
        
        // 方法2：假设通过用户创建的学生记录来判断（需要保证用户只能创建自己学院的记录）
        List<StudentProfile> createdProfiles = studentProfileRepository.findByCreatedBy(user);
        if (!createdProfiles.isEmpty()) {
            return createdProfiles.get(0).getCollege();
        }
        
        // 如果无法确定，返回null
        return null;
    }
    
    /**
     * 检查用户是否拥有指定角色
     *
     * @param user 用户
     * @param roleName 角色名称
     * @return 是否拥有该角色
     */
    private boolean hasRole(User user, String roleName) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * 根据条件查询学生学籍信息
     *
     * @param collegeId 学院ID
     * @param majorId 专业ID
     * @param keyword 关键词
     * @param pageable 分页信息
     * @return 学生学籍信息分页列表
     */
    private Page<StudentProfile> queryStudentProfiles(Long collegeId, Long majorId, String keyword, Pageable pageable) {
        // 按学院筛选
        if (collegeId != null) {
            College college = collegeRepository.findById(collegeId)
                    .orElseThrow(() -> new RuntimeException("学院不存在"));
            
            // 按专业筛选
            if (majorId != null) {
                Major major = majorRepository.findById(majorId)
                        .orElseThrow(() -> new RuntimeException("专业不存在"));
                
                // 按专业和关键词筛选
                if (keyword != null && !keyword.isEmpty()) {
                    return studentProfileRepository.findByMajorAndNameContainingOrStudentIdContaining(
                            major, keyword, keyword, pageable);
                } else {
                    return studentProfileRepository.findByMajor(major, pageable);
                }
            }
            
            // 按学院和关键词筛选
            if (keyword != null && !keyword.isEmpty()) {
                return studentProfileRepository.searchByCollegeAndNameOrStudentId(college, keyword, pageable);
            } else {
                return studentProfileRepository.findByCollege(college, pageable);
            }
        }
        
        // 只按关键词筛选
        if (keyword != null && !keyword.isEmpty()) {
            return studentProfileRepository.searchByNameOrStudentId(keyword, pageable);
        }
        
        // 不筛选，返回全部
        return studentProfileRepository.findAll(pageable);
    }
    
    /**
     * 根据名称查找学院
     *
     * @param name 学院名称
     * @return 学院对象
     */
    private College findCollegeByName(String name) {
        return collegeRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("学院不存在: " + name));
    }
    
    /**
     * 根据名称和所属学院查找专业
     *
     * @param name 专业名称
     * @param college 所属学院
     * @return 专业对象
     */
    private Major findMajorByNameAndCollege(String name, College college) {
        return majorRepository.findByNameAndCollege(name, college)
                .orElseThrow(() -> new IllegalArgumentException("专业不存在: " + name));
    }
    
    /**
     * 根据姓名查找导师
     *
     * @param name 导师姓名
     * @return 导师用户对象
     */
    private User findSupervisorByName(String name) {
        return userRepository.findByNameAndRoles_Name(name, "ROLE_TEACHER")
                .orElseThrow(() -> new IllegalArgumentException("导师不存在: " + name));
    }
    
    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @return LocalDate对象
     */
    private LocalDate parseDate(String dateStr) {
        // 支持多种日期格式
        String[] patterns = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd"};
        for (String pattern : patterns) {        try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception e) {
                // 继续尝试其他格式
            }
        }
        throw new IllegalArgumentException("日期格式不正确: " + dateStr);
    }
    
    /**
     * 从Excel单元格获取字符串值
     *
     * @param row 行对象
     * @param columnIndex 列索引
     * @return 单元格字符串值
     */
    private String getCellValueAsString(Row row, Integer columnIndex) {
        if (columnIndex == null || row == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // 如果是日期格式
                    return cell.getDateCellValue().toString();
                } else {
                    // 如果是数字，转换为字符串并去掉小数点
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == (long) numericValue) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BLANK:
            case _NONE:
            default:
                return null;
        }
    }
}
