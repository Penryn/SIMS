package com.graduate.management.controller;

import com.graduate.management.dto.ApiResponse;
import com.graduate.management.dto.SystemLogDto;
import com.graduate.management.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 审计日志管理控制器
 * 主要用于日志查询和审计
 */
@RestController
@RequestMapping("/api/system-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ROLE_AUDIT_ADMIN', 'ROLE_ADMIN', 'ROLE_SYSTEM_ADMIN')")
public class SystemLogController {
    
    private final SystemLogService systemLogService;
    
    /**
     * 获取所有日志（分页）
     *
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 日志分页列表
     */
    @GetMapping
    public ApiResponse<Page<SystemLogDto>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = "asc".equalsIgnoreCase(sortDir) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SystemLogDto> logs = systemLogService.getAllLogs(pageable);
        
        return ApiResponse.success("查询成功", logs);
    }
    
    /**
     * 根据ID获取日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SystemLogDto> getLogById(@PathVariable Long id) {
        try {
            SystemLogDto log = systemLogService.getLogById(id);
            return ApiResponse.success("查询成功", log);
        } catch (Exception e) {
            return ApiResponse.fail("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID查询日志
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 日志分页列表
     */
    @GetMapping("/by-user/{userId}")
    public ApiResponse<Page<SystemLogDto>> getLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SystemLogDto> logs = systemLogService.getLogsByUser(userId, pageable);
        
        return ApiResponse.success("查询成功", logs);
    }
    
    /**
     * 根据操作类型查询日志
     *
     * @param operation 操作类型
     * @param page 页码
     * @param size 每页大小
     * @return 日志分页列表
     */
    @GetMapping("/by-operation/{operation}")
    public ApiResponse<Page<SystemLogDto>> getLogsByOperation(
            @PathVariable String operation,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SystemLogDto> logs = systemLogService.getLogsByOperation(operation, pageable);
        
        return ApiResponse.success("查询成功", logs);
    }
    
    /**
     * 根据资源类型查询日志
     *
     * @param resourceType 资源类型
     * @param page 页码
     * @param size 每页大小
     * @return 日志分页列表
     */
    @GetMapping("/by-resource-type/{resourceType}")
    public ApiResponse<Page<SystemLogDto>> getLogsByResourceType(
            @PathVariable String resourceType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SystemLogDto> logs = systemLogService.getLogsByResourceType(resourceType, pageable);
        
        return ApiResponse.success("查询成功", logs);
    }
    
    /**
     * 根据日期范围查询日志
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param size 每页大小
     * @return 日志分页列表
     */
    @GetMapping("/by-date-range")
    public ApiResponse<Page<SystemLogDto>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SystemLogDto> logs = systemLogService.getLogsByDateRange(startDate, endDate, pageable);
        
        return ApiResponse.success("查询成功", logs);
    }
    
    /**
     * 高级搜索日志
     *
     * @param keyword 关键词
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 日志分页列表
     */
    @GetMapping("/search")
    public ApiResponse<Page<SystemLogDto>> searchLogs(
            @RequestParam String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(6);
        }
        
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<SystemLogDto> logs = systemLogService.searchLogs(keyword, startDate, endDate, pageable);
        
        return ApiResponse.success("查询成功", logs);
    }
    
    /**
     * 验证日志完整性
     *
     * @param id 日志ID
     * @return 验证结果
     */
    @GetMapping("/{id}/verify")
    public ApiResponse<Boolean> verifyLogIntegrity(@PathVariable Long id) {
        try {
            boolean result = systemLogService.verifyLogIntegrity(id);
            if (result) {
                return ApiResponse.success("日志完整性验证通过", true);
            } else {
                return ApiResponse.fail("日志完整性验证失败，日志可能已被篡改");
            }
        } catch (Exception e) {
            return ApiResponse.fail("验证失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证所有日志的完整性
     *
     * @return 验证结果
     */
    @GetMapping("/verify-all")
    public ApiResponse<?> validateAllLogs() {
        try {
            systemLogService.validateAllLogs();
            return ApiResponse.success("所有日志完整性验证已启动，请查看系统日志了解详情");
        } catch (Exception e) {
            return ApiResponse.fail("验证启动失败: " + e.getMessage());
        }
    }
}
