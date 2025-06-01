package com.graduate.management.service;

import com.graduate.management.dto.SystemLogDto;
import com.graduate.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

public interface SystemLogService {
    
    void log(String operation, String resourceType, Long resourceId, User user, 
             String details, Boolean success, String errorMessage, HttpServletRequest request);
    
    SystemLogDto getLogById(Long id);
    
    Page<SystemLogDto> getAllLogs(Pageable pageable);
    
    Page<SystemLogDto> getLogsByUser(Long userId, Pageable pageable);
    
    Page<SystemLogDto> getLogsByOperation(String operation, Pageable pageable);
    
    Page<SystemLogDto> getLogsByResourceType(String resourceType, Pageable pageable);
    
    Page<SystemLogDto> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Page<SystemLogDto> searchLogs(String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    boolean verifyLogIntegrity(Long id);
    
    void validateAllLogs();
}
