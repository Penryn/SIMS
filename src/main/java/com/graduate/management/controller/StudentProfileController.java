package com.graduate.management.controller;

import com.graduate.management.dto.ApiResponse;
import com.graduate.management.dto.StudentProfileDto;
import com.graduate.management.entity.StudentProfile;
import com.graduate.management.entity.User;
import com.graduate.management.security.UserDetailsImpl;
import com.graduate.management.service.StudentProfileService;
import com.graduate.management.service.SystemLogService;
import com.graduate.management.service.UserService;
import com.graduate.management.util.DtoMaskUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * 学生学籍信息管理控制器
 */
@Tag(name = "学生学籍管理", description = "学生学籍信息管理相关接口")
@RestController
@RequestMapping("/api/student-profiles")
@RequiredArgsConstructor
public class StudentProfileController {
    
    private final StudentProfileService studentProfileService;
    private final UserService userService;
    private final SystemLogService systemLogService;
    private final DtoMaskUtil dtoMaskUtil;
    
    /**
     * 获取学生学籍信息列表（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @param collegeId 学院ID（可选）
     * @param majorId 专业ID（可选）
     * @param keyword 关键词（可选）
     * @return 学生学籍信息分页列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GRADUATE_ADMIN', 'ROLE_COLLEGE_ADMIN', 'ROLE_COLLEGE_SECRETARY')")
    public ApiResponse<Page<StudentProfileDto>> getAllStudentProfiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long collegeId,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) String keyword) {
        
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username).orElse(null);
        
        // 构建分页和排序
        Sort sort = "asc".equalsIgnoreCase(sortDir) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // 根据用户角色和请求参数获取学生学籍信息
        Page<StudentProfileDto> studentProfiles = studentProfileService.getStudentProfiles(
                currentUser, collegeId, majorId, keyword, pageable);
        
        return ApiResponse.success("查询成功", studentProfiles);
    }
    
    /**
     * 根据ID获取学生学籍信息详情
     * 
     * @param id 学生学籍ID
     * @param request HTTP请求
     * @return 学生学籍信息详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GRADUATE_ADMIN', 'ROLE_COLLEGE_ADMIN', 'ROLE_COLLEGE_SECRETARY', 'ROLE_TEACHER') or #userDetails.user.username == @studentProfileService.findById(#id).student.username")
    public ApiResponse<StudentProfileDto> getStudentProfileById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            StudentProfile studentProfile = studentProfileService.findById(id);
            StudentProfileDto profileDto = studentProfileService.convertToDto(studentProfile);
            
            // 记录查看学籍信息日志
            systemLogService.log("VIEW", "STUDENT_PROFILE", id, userDetails.getUser(),
                    "查看学生学籍信息: " + studentProfile.getStudentId(), true, null, request);
            
            // 根据用户角色进行脱敏处理
            if (userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_GRADUATE_ADMIN")
                            || auth.getAuthority().equals("ROLE_COLLEGE_ADMIN")
                            || auth.getAuthority().equals("ROLE_COLLEGE_SECRETARY"))) {
                // 管理员不脱敏
                return ApiResponse.success("查询成功", profileDto);
            } else {
                // 其他角色，包括学生本人和导师，对部分信息脱敏
                return ApiResponse.success("查询成功", dtoMaskUtil.maskStudentProfileDto(profileDto));
            }
        } catch (Exception e) {
            return ApiResponse.fail("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建学生学籍信息
     * 
     * @param studentProfile 学生学籍信息
     * @param request HTTP请求
     * @return 创建结果
     */    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_COLLEGE_SECRETARY', 'ROLE_COLLEGE_ADMIN', 'ROLE_GRADUATE_ADMIN')")
    public ApiResponse<StudentProfileDto> createStudentProfile(
            @Valid @RequestBody StudentProfileDto studentProfileDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            // 创建学生学籍信息
            StudentProfileDto createdProfile = studentProfileService.createStudentProfile(studentProfileDto);
            
            // 记录创建学籍信息日志
            systemLogService.log("CREATE", "STUDENT_PROFILE", createdProfile.getId(), userDetails.getUser(),
                    "创建学生学籍信息: " + createdProfile.getStudentId(), true, null, request);
            
            return ApiResponse.success("创建成功", createdProfile);
        } catch (Exception e) {
            return ApiResponse.fail("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新学生学籍信息
     * 
     * @param id 学生学籍ID
     * @param studentProfile 学生学籍信息
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_GRADUATE_ADMIN') or (hasAnyRole('ROLE_COLLEGE_SECRETARY', 'ROLE_COLLEGE_ADMIN') and @studentProfileService.isFromSameCollege(#id, #userDetails.user)) or #userDetails.user.username == @studentProfileService.findById(#id).student.username")
    public ApiResponse<StudentProfileDto> updateStudentProfile(
            @PathVariable Long id,
            @Valid @RequestBody StudentProfile studentProfile,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            StudentProfile existingProfile = studentProfileService.findById(id);
            
            // 根据用户角色决定更新方式
            if (userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT"))) {
                // 学生只能修改除基础信息外的个人信息
                studentProfile = studentProfileService.updateStudentProfileByStudent(
                        existingProfile, studentProfile);
            } else if (userDetails.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_COLLEGE_SECRETARY")
                            || auth.getAuthority().equals("ROLE_COLLEGE_ADMIN"))) {
                // 学院管理员可以修改基础信息，但需要研究生院管理员审核
                studentProfile.setId(id);
                studentProfile = studentProfileService.updateStudentProfileWithApproval(
                        studentProfile, userDetails.getUser());
            } else {
                // 研究生院管理员可以直接修改全部信息
                studentProfile.setId(id);
                studentProfile = studentProfileService.updateStudentProfile(studentProfile);
            }
            
            // 记录更新学籍信息日志
            systemLogService.log("UPDATE", "STUDENT_PROFILE", studentProfile.getId(), userDetails.getUser(),
                    "更新学生学籍信息: " + studentProfile.getStudentId(), true, null, request);
            
            return ApiResponse.success("更新成功", studentProfileService.convertToDto(studentProfile));
        } catch (Exception e) {
            return ApiResponse.fail("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量导入学生学籍信息
     * 
     * @param studentProfiles 学生学籍信息列表
     * @param request HTTP请求
     * @return 导入结果
     */    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ROLE_COLLEGE_SECRETARY', 'ROLE_COLLEGE_ADMIN', 'ROLE_GRADUATE_ADMIN')")
    public ApiResponse<List<StudentProfileDto>> batchImportStudentProfiles(
            @Valid @RequestBody List<StudentProfile> studentProfiles,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            // 设置创建者信息
            for (StudentProfile profile : studentProfiles) {
                if (profile.getCreatedBy() == null) {
                    profile.setCreatedBy(userDetails.getUser());
                }
            }
            
            // 批量创建学生学籍信息
            List<StudentProfile> createdProfiles = studentProfileService.batchCreateStudentProfiles(studentProfiles);
            
            // 记录批量导入学籍信息日志
            systemLogService.log("BATCH_IMPORT", "STUDENT_PROFILE", null, userDetails.getUser(),
                    "批量导入学生学籍信息，共" + createdProfiles.size() + "条", true, null, request);
            
            // 转换为DTO返回
            List<StudentProfileDto> profileDtos = createdProfiles.stream()
                    .map(studentProfileService::convertToDto)
                    .collect(java.util.stream.Collectors.toList());
            
            return ApiResponse.success("批量导入成功", profileDtos);
        } catch (Exception e) {
            return ApiResponse.fail("批量导入失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前登录学生的学籍信息
     * 
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 学生学籍信息
     */
    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<StudentProfileDto> getMyStudentProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            StudentProfile studentProfile = studentProfileService.findByStudent(userDetails.getUser());
            
            // 记录查看个人学籍信息日志
            systemLogService.log("VIEW", "STUDENT_PROFILE", studentProfile.getId(), userDetails.getUser(),
                    "查看个人学籍信息", true, null, request);
            
            return ApiResponse.success("查询成功", studentProfileService.convertToDto(studentProfile));
        } catch (Exception e) {
            return ApiResponse.fail("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新当前登录学生的学籍信息
     * 
     * @param studentProfile 学生学籍信息
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/my-profile")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ApiResponse<StudentProfileDto> updateMyStudentProfile(
            @Valid @RequestBody StudentProfile studentProfile,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            StudentProfile existingProfile = studentProfileService.findByStudent(userDetails.getUser());
            
            // 学生只能修改除基础信息外的个人信息
            StudentProfile updatedProfile = studentProfileService.updateStudentProfileByStudent(
                    existingProfile, studentProfile);
            
            // 记录更新个人学籍信息日志
            systemLogService.log("UPDATE", "STUDENT_PROFILE", updatedProfile.getId(), userDetails.getUser(),
                    "更新个人学籍信息", true, null, request);
            
            return ApiResponse.success("更新成功", studentProfileService.convertToDto(updatedProfile));
        } catch (Exception e) {
            return ApiResponse.fail("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 通过Excel文件批量导入学生学籍信息
     *
     * @param file Excel文件
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 导入结果
     */
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ROLE_COLLEGE_SECRETARY', 'ROLE_COLLEGE_ADMIN', 'ROLE_GRADUATE_ADMIN')")
    public ApiResponse<String> importStudentProfilesFromExcel(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.fail("文件不能为空");
            }
            
            // 调用服务导入学生学籍信息
            boolean success = studentProfileService.importStudentProfiles(file);
            
            // 记录导入日志
            systemLogService.log("IMPORT_EXCEL", "STUDENT_PROFILE", null, userDetails.getUser(),
                    "从Excel文件导入学生学籍信息：" + file.getOriginalFilename(), success, 
                    success ? null : "导入失败", request);
            
            if (success) {
                return ApiResponse.success("导入成功");
            } else {
                return ApiResponse.fail("导入失败：文件格式可能不正确或数据有误");
            }
        } catch (Exception e) {
            // 记录导入异常
            systemLogService.log("IMPORT_EXCEL", "STUDENT_PROFILE", null, userDetails.getUser(),
                    "从Excel文件导入学生学籍信息异常：" + file.getOriginalFilename(),
                    false, "导入异常: " + e.getMessage(), request);
            
            return ApiResponse.fail("导入失败: " + e.getMessage());
        }
    }
}
