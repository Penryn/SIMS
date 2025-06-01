package com.graduate.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemLogDto {
    
    private Long id;
    
    private String operation;
    
    private String resourceType;
    
    private Long resourceId;
    
    private Long userId;
    
    private String username;
    
    private String ipAddress;
    
    private String details;
    
    private Boolean success;
    
    private String errorMessage;
    
    private String hmacValue;
    
    private LocalDateTime createdAt;
}
