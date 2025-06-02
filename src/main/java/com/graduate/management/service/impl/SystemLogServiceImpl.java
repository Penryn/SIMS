package com.graduate.management.service.impl;

import com.graduate.management.dto.SystemLogDto;
import com.graduate.management.entity.SystemLog;
import com.graduate.management.entity.User;
import com.graduate.management.repository.SystemLogRepository;
import com.graduate.management.service.SystemLogService;
import com.graduate.management.util.SM3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SystemLogRepository systemLogRepository;
    private final SM3Util sm3Util;
    
    @Value("${system.log.hmac-key:logSecurityKey}")
    private String hmacKey;

    @Override
    @Transactional
    public void log(String operation, String resourceType, Long resourceId, User user,
                   String details, Boolean success, String errorMessage, HttpServletRequest request) {
        
        SystemLog log = new SystemLog();
        log.setOperation(operation);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setUser(user);
        log.setIpAddress(getClientIpAddress(request));
        log.setDetails(details);
        log.setSuccess(success);
        log.setErrorMessage(errorMessage);
        
        // 使用HMAC-SM3计算完整性校验值
        String logData = buildLogData(log);
        log.setHmacValue(sm3Util.hmac(logData, hmacKey));
        
        systemLogRepository.save(log);
    }

    @Override
    public SystemLogDto getLogById(Long id) {
        SystemLog log = systemLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("日志记录不存在"));
        
        return convertToDto(log);
    }

    @Override
    public Page<SystemLogDto> getAllLogs(Pageable pageable) {
        return systemLogRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<SystemLogDto> getLogsByUser(Long userId, Pageable pageable) {
        // 实际应用中，可以根据用户ID查询日志
        // 这里简化处理，使用关键字搜索
        return searchLogs(userId.toString(), LocalDateTime.now().minusMonths(6), LocalDateTime.now(), pageable);
    }

    @Override
    public Page<SystemLogDto> getLogsByOperation(String operation, Pageable pageable) {
        // 实际应用中，可以根据操作类型查询日志
        // 这里简化处理，使用关键字搜索
        return searchLogs(operation, LocalDateTime.now().minusMonths(6), LocalDateTime.now(), pageable);
    }

    @Override
    public Page<SystemLogDto> getLogsByResourceType(String resourceType, Pageable pageable) {
        // 实际应用中，可以根据资源类型查询日志
        // 这里简化处理，使用关键字搜索
        return searchLogs(resourceType, LocalDateTime.now().minusMonths(6), LocalDateTime.now(), pageable);
    }

    @Override
    public Page<SystemLogDto> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return systemLogRepository.findByCreatedAtBetween(startDate, endDate, pageable)
                .map(this::convertToDto);
    }

    @Override
    public Page<SystemLogDto> searchLogs(String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return systemLogRepository.searchLogs(keyword, startDate, endDate, pageable)
                .map(this::convertToDto);
    }

    @Override
    public boolean verifyLogIntegrity(Long id) {
        SystemLog log = systemLogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("日志记录不存在"));
        
        String originalHmac = log.getHmacValue();
        String logData = buildLogData(log);
        String calculatedHmac = sm3Util.hmac(logData, hmacKey);
        
        return originalHmac.equals(calculatedHmac);
    }
    
    @Transactional(readOnly = true)
    public List<Long> verifyAllLogs() {
        List<Long> tamperedLogs = new ArrayList<>();
        List<SystemLog> logs = systemLogRepository.findAll();
        
        for (SystemLog log : logs) {
            String originalHmac = log.getHmacValue();
            String logData = buildLogData(log);
            String calculatedHmac = sm3Util.hmac(logData, hmacKey);
            
            if (!originalHmac.equals(calculatedHmac)) {
                tamperedLogs.add(log.getId());
            }
        }
        
        return tamperedLogs;
    }
    
    @Override
    public void validateAllLogs() {
        List<SystemLog> logs = systemLogRepository.findAll();
        List<SystemLog> invalidLogs = new ArrayList<>();
        
        for (SystemLog log : logs) {
            if (!verifyLogIntegrity(log.getId())) {
                invalidLogs.add(log);
            }
        }
        
        // 可以根据需求决定是记录无效日志，或者发送警报等
        if (!invalidLogs.isEmpty()) {
            // 记录警告信息
            log("日志验证", "SystemLog", null, null, 
                "日志完整性验证发现问题，共有" + invalidLogs.size() + "条日志可能被篡改", 
                false, "日志完整性验证失败", null);
        }
    }

    // 将实体转换为DTO
    private SystemLogDto convertToDto(SystemLog log) {
        SystemLogDto dto = new SystemLogDto();
        dto.setId(log.getId());
        dto.setOperation(log.getOperation());
        dto.setResourceType(log.getResourceType());
        dto.setResourceId(log.getResourceId());
        
        if (log.getUser() != null) {
            dto.setUserId(log.getUser().getId());
            dto.setUsername(log.getUser().getUsername());
        }
        
        dto.setIpAddress(log.getIpAddress());
        dto.setDetails(log.getDetails());
        dto.setSuccess(log.getSuccess());
        dto.setErrorMessage(log.getErrorMessage());
        dto.setHmacValue(log.getHmacValue());
        dto.setCreatedAt(log.getCreatedAt());
        
        return dto;
    }
    
    // 构建日志数据，用于HMAC计算
    private String buildLogData(SystemLog log) {
        StringBuilder sb = new StringBuilder();
        sb.append(log.getId() != null ? log.getId() : "");
        sb.append(log.getOperation() != null ? log.getOperation() : "");
        sb.append(log.getResourceType() != null ? log.getResourceType() : "");
        sb.append(log.getResourceId() != null ? log.getResourceId() : "");
        sb.append(log.getUser() != null ? log.getUser().getId() : "");
        sb.append(log.getIpAddress() != null ? log.getIpAddress() : "");
        sb.append(log.getDetails() != null ? log.getDetails() : "");
        sb.append(log.getSuccess() != null ? log.getSuccess() : "");
        sb.append(log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");
        
        return sb.toString();
    }
    
    // 获取客户端IP地址
    private String getClientIpAddress(HttpServletRequest request) {
        if (request != null) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        }
        return "unknown";
    }

    private String getClientIp(HttpServletRequest request) {
        if (request != null) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_CLIENT_IP");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        }
        return "unknown";
    }
}
