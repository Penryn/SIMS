package com.graduate.management.controller;

import com.graduate.management.dto.ApiResponse;
import com.graduate.management.dto.StudentProfileDto;
import com.graduate.management.entity.StudentProfile;
import com.graduate.management.entity.User;
import com.graduate.management.security.UserDetailsImpl;
import com.graduate.management.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 导师-学生关系管理控制器
 */
@RestController
@RequestMapping("/api/supervisors")
@RequiredArgsConstructor
public class SupervisorController {
    
    private final StudentProfileService studentProfileService;
    
    /**
     * 获取当前导师指导的所有学生
     *
     * @param userDetails 当前登录用户
     * @return 学生列表
     */
    @GetMapping("/students")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentProfileDto>>> getStudents(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User currentUser = userDetails.getUser();
        List<StudentProfile> students = studentProfileService.findBySupervisor(currentUser);
        List<StudentProfileDto> studentDtos = students.stream()
                .map(student -> studentProfileService.convertToDto(student))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ApiResponse<>(true, "获取学生列表成功", studentDtos));
    }
    
    /**
     * 获取特定学生的详细信息
     *
     * @param userDetails 当前登录用户
     * @param studentId   学生ID
     * @return 学生详情
     */
    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<StudentProfileDto>> getStudentDetail(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable String studentId) {
        User currentUser = userDetails.getUser();
        StudentProfile student = studentProfileService.findByStudentId(studentId);
        
        // 验证该学生是否是该导师指导的学生
        if (!student.getSupervisor().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "您无权查看此学生信息", null));
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "获取学生详情成功", 
                studentProfileService.convertToDto(student)));
    }
}
