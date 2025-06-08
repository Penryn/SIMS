package com.graduate.management.controller;

import com.graduate.management.dto.ApiResponse;
import com.graduate.management.entity.College;
import com.graduate.management.entity.Major;
import com.graduate.management.security.UserDetailsImpl;
import com.graduate.management.service.CollegeService;
import com.graduate.management.service.MajorService;
import com.graduate.management.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 学院和专业管理控制器
 */
@RestController
@RequiredArgsConstructor
public class CollegeController {
    
    private final CollegeService collegeService;
    private final MajorService majorService;
    private final SystemLogService systemLogService;
    
    /**
     * 获取所有学院
     *
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 学院分页列表
     */
    @GetMapping("/api/colleges")
    public ApiResponse<Page<College>> getAllColleges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = "asc".equalsIgnoreCase(sortDir) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<College> colleges = collegeService.getAllColleges(pageable);
        
        return ApiResponse.success("查询成功", colleges);
    }
    
    /**
     * 获取所有学院（不分页）
     *
     * @return 学院列表
     */
    @GetMapping("/api/colleges/all")
    public ApiResponse<List<College>> getAllCollegesWithoutPaging() {
        List<College> colleges = collegeService.getAllColleges();
        return ApiResponse.success("查询成功", colleges);
    }
    
    /**
     * 根据ID获取学院
     *
     * @param id 学院ID
     * @return 学院信息
     */    @GetMapping("/api/colleges/{id}")
    public ApiResponse<College> getCollegeById(@PathVariable Long id) {
        Optional<College> collegeOpt = collegeService.getCollegeById(id);
        if (collegeOpt.isPresent()) {
            return ApiResponse.success("查询成功", collegeOpt.get());
        } else {
            return ApiResponse.fail("学院不存在");
        }
    }
    
    /**
     * 创建学院
     *
     * @param college 学院信息
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping("/api/colleges")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GRADUATE_ADMIN')")
    public ApiResponse<College> createCollege(
            @Valid @RequestBody College college,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            College createdCollege = collegeService.createCollege(college);
            
            // 记录创建学院日志
            systemLogService.log("CREATE", "COLLEGE", createdCollege.getId(), userDetails.getUser(),
                    "创建学院: " + createdCollege.getName(), true, null, request);
            
            return ApiResponse.success("创建成功", createdCollege);
        } catch (Exception e) {
            return ApiResponse.fail("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新学院
     *
     * @param id 学院ID
     * @param college 学院信息
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/api/colleges/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GRADUATE_ADMIN')")
    public ApiResponse<College> updateCollege(
            @PathVariable Long id,
            @Valid @RequestBody College college,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {        try {
            College updatedCollege = collegeService.updateCollege(id, college);
            
            // 记录更新学院日志
            systemLogService.log("UPDATE", "COLLEGE", updatedCollege.getId(), userDetails.getUser(),
                    "更新学院: " + updatedCollege.getName(), true, null, request);
            
            return ApiResponse.success("更新成功", updatedCollege);
        } catch (Exception e) {
            return ApiResponse.fail("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除学院
     *
     * @param id 学院ID
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/api/colleges/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> deleteCollege(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {        try {
            Optional<College> collegeOpt = collegeService.getCollegeById(id);
            if (!collegeOpt.isPresent()) {
                return ApiResponse.fail("学院不存在");
            }
            College college = collegeOpt.get();
            collegeService.deleteCollege(id);
            
            // 记录删除学院日志
            systemLogService.log("DELETE", "COLLEGE", id, userDetails.getUser(),
                    "删除学院: " + college.getName(), true, null, request);
            
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.fail("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取学院下的所有专业
     *
     * @param collegeId 学院ID
     * @return 专业列表
     */
    @GetMapping("/api/colleges/{collegeId}/majors")
    public ApiResponse<List<Major>> getMajorsByCollege(@PathVariable Long collegeId) {
        List<Major> majors = majorService.getMajorsByCollege(collegeId);
        return ApiResponse.success("查询成功", majors);
    }
    
    /**
     * 获取所有专业（分页）
     *
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 专业分页列表
     */
    @GetMapping("/api/majors")
    public ApiResponse<Page<Major>> getAllMajors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = "asc".equalsIgnoreCase(sortDir) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Major> majors = majorService.getAllMajors(pageable);
        
        return ApiResponse.success("查询成功", majors);
    }
    
    /**
     * 获取所有专业（不分页）
     *
     * @return 专业列表
     */
    @GetMapping("/api/majors/all")
    public ApiResponse<List<Major>> getAllMajorsWithoutPaging() {
        List<Major> majors = majorService.getAllMajors();
        return ApiResponse.success("查询成功", majors);
    }
    
    /**
     * 根据ID获取专业
     *
     * @param id 专业ID
     * @return 专业信息
     */    @GetMapping("/api/majors/{id}")
    public ApiResponse<Major> getMajorById(@PathVariable Long id) {
        Optional<Major> majorOpt = majorService.getMajorById(id);
        if (majorOpt.isPresent()) {
            return ApiResponse.success("查询成功", majorOpt.get());
        } else {
            return ApiResponse.fail("专业不存在");
        }
    }
    
    /**
     * 创建专业
     *
     * @param major 专业信息
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping("/api/majors")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GRADUATE_ADMIN')")
    public ApiResponse<Major> createMajor(
            @Valid @RequestBody Major major,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            Major createdMajor = majorService.createMajor(major);
            
            // 记录创建专业日志
            systemLogService.log("CREATE", "MAJOR", createdMajor.getId(), userDetails.getUser(),
                    "创建专业: " + createdMajor.getName(), true, null, request);
            
            return ApiResponse.success("创建成功", createdMajor);
        } catch (Exception e) {
            return ApiResponse.fail("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新专业
     *
     * @param id 专业ID
     * @param major 专业信息
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 更新结果
     */
    @PutMapping("/api/majors/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_GRADUATE_ADMIN')")
    public ApiResponse<Major> updateMajor(
            @PathVariable Long id,
            @Valid @RequestBody Major major,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {        try {
            Major updatedMajor = majorService.updateMajor(id, major);
            
            // 记录更新专业日志
            systemLogService.log("UPDATE", "MAJOR", updatedMajor.getId(), userDetails.getUser(),
                    "更新专业: " + updatedMajor.getName(), true, null, request);
            
            return ApiResponse.success("更新成功", updatedMajor);
        } catch (Exception e) {
            return ApiResponse.fail("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除专业
     *
     * @param id 专业ID
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 删除结果
     */
    @DeleteMapping("/api/majors/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ApiResponse<?> deleteMajor(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {        try {
            Optional<Major> majorOpt = majorService.getMajorById(id);
            if (!majorOpt.isPresent()) {
                return ApiResponse.fail("专业不存在");
            }
            Major major = majorOpt.get();
            majorService.deleteMajor(id);
            
            // 记录删除专业日志
            systemLogService.log("DELETE", "MAJOR", id, userDetails.getUser(),
                    "删除专业: " + major.getName(), true, null, request);
            
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.fail("删除失败: " + e.getMessage());
        }
    }
}
