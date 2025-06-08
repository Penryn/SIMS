package com.graduate.management.controller;

import com.graduate.management.dto.ApiResponse;
import com.graduate.management.dto.ChangeRequestDto;
import com.graduate.management.entity.ChangeRequest;
import com.graduate.management.security.UserDetailsImpl;
import com.graduate.management.service.ChangeRequestService;
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

/**
 * 变更申请与审核控制器
 */
@RestController
@RequestMapping("/api/change-requests")
@RequiredArgsConstructor
public class ChangeRequestController {
    
    private final ChangeRequestService changeRequestService;
    private final SystemLogService systemLogService;
    
    /**
     * 获取所有变更申请（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @param status 状态（可选）
     * @return 变更申请分页列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_GRADUATE_ADMIN')")
    public ApiResponse<Page<ChangeRequestDto>> getAllChangeRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status) {
        
        Sort sort = "asc".equalsIgnoreCase(sortDir) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ChangeRequestDto> changeRequests = changeRequestService.getAllChangeRequests(status, pageable);
        
        return ApiResponse.success("查询成功", changeRequests);
    }
    
    /**
     * 获取当前学院的变更申请（分页）
     * 
     * @param userDetails 当前登录用户
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @param status 状态（可选）
     * @return 变更申请分页列表
     */
    @GetMapping("/college")
    @PreAuthorize("hasAnyRole('ROLE_COLLEGE_ADMIN', 'ROLE_COLLEGE_SECRETARY')")
    public ApiResponse<Page<ChangeRequestDto>> getCollegeChangeRequests(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status) {
        
        Sort sort = "asc".equalsIgnoreCase(sortDir) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ChangeRequestDto> changeRequests = changeRequestService.getCollegeChangeRequests(
                userDetails.getUser(), status, pageable);
        
        return ApiResponse.success("查询成功", changeRequests);
    }
    
    /**
     * 根据ID获取变更申请详情
     * 
     * @param id 变更申请ID
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 变更申请详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_GRADUATE_ADMIN', 'ROLE_COLLEGE_ADMIN', 'ROLE_COLLEGE_SECRETARY')")
    public ApiResponse<ChangeRequestDto> getChangeRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            ChangeRequestDto changeRequest = changeRequestService.getChangeRequestById(id, userDetails.getUser());
            
            // 记录查看变更申请日志
            systemLogService.log("VIEW", "CHANGE_REQUEST", id, userDetails.getUser(),
                    "查看变更申请详情", true, null, request);
            
            return ApiResponse.success("查询成功", changeRequest);
        } catch (Exception e) {
            return ApiResponse.fail("查询失败: " + e.getMessage());
        }
    }
      /**
     * 创建变更申请
     * 
     * @param changeRequestDto 变更申请DTO
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 创建结果
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_COLLEGE_SECRETARY', 'ROLE_COLLEGE_ADMIN')")
    public ApiResponse<ChangeRequestDto> createChangeRequest(
            @Valid @RequestBody ChangeRequestDto changeRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            // 设置申请人ID
            changeRequestDto.setRequesterId(userDetails.getUser().getId());
            
            // 创建变更申请
            ChangeRequestDto createdRequest = changeRequestService.createChangeRequest(changeRequestDto);
            
            // 记录创建变更申请日志
            systemLogService.log("CREATE", "CHANGE_REQUEST", createdRequest.getId(), userDetails.getUser(),
                    "创建变更申请: " + (createdRequest.getFieldName() != null ? "修改" + createdRequest.getFieldName() + "申请" : "变更申请"), true, null, request);
            
            return ApiResponse.success("创建成功", createdRequest);
        } catch (Exception e) {
            return ApiResponse.fail("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 审核变更申请
     * 
     * @param id 变更申请ID
     * @param action 审核动作（approve/reject）
     * @param comment 审核意见
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 审核结果
     */
    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('ROLE_GRADUATE_ADMIN')")
    public ApiResponse<ChangeRequestDto> reviewChangeRequest(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            if (!action.equals("approve") && !action.equals("reject")) {
                return ApiResponse.fail("无效的审核动作，必须为approve或reject");
            }
            
            ChangeRequest changeRequest;
            if (action.equals("approve")) {
                changeRequest = changeRequestService.approveChangeRequest(id, userDetails.getUser(), comment);
                
                // 记录审批变更申请日志
                systemLogService.log("APPROVE", "CHANGE_REQUEST", id, userDetails.getUser(),
                        "审批通过变更申请", true, null, request);
            } else {
                changeRequest = changeRequestService.rejectChangeRequest(id, userDetails.getUser(), comment);
                
                // 记录驳回变更申请日志
                systemLogService.log("REJECT", "CHANGE_REQUEST", id, userDetails.getUser(),
                        "驳回变更申请", true, null, request);
            }
            
            return ApiResponse.success("审核成功", changeRequestService.convertToDto(changeRequest));
        } catch (Exception e) {
            return ApiResponse.fail("审核失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消变更申请
     * 
     * @param id 变更申请ID
     * @param userDetails 当前登录用户
     * @param request HTTP请求
     * @return 取消结果
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_COLLEGE_SECRETARY', 'ROLE_COLLEGE_ADMIN')")
    public ApiResponse<ChangeRequestDto> cancelChangeRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            HttpServletRequest request) {
        try {
            ChangeRequest changeRequest = changeRequestService.cancelChangeRequest(id, userDetails.getUser());
            
            // 记录取消变更申请日志
            systemLogService.log("CANCEL", "CHANGE_REQUEST", id, userDetails.getUser(),
                    "取消变更申请", true, null, request);
            
            return ApiResponse.success("取消成功", changeRequestService.convertToDto(changeRequest));
        } catch (Exception e) {
            return ApiResponse.fail("取消失败: " + e.getMessage());
        }
    }
}
