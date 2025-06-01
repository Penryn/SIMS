package com.graduate.management.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_logs")
public class SystemLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String operation; // 操作类型：LOGIN, LOGOUT, VIEW, CREATE, UPDATE, DELETE
    
    @Column(nullable = false)
    private String resourceType; // 资源类型：USER, STUDENT_PROFILE, COLLEGE, etc.
    
    private Long resourceId; // 资源ID
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // 操作用户
    
    @Column(nullable = false)
    private String ipAddress; // 操作IP
    
    @Column(length = 1000)
    private String details; // 操作详情
    
    private Boolean success; // 操作是否成功
    
    @Column(length = 500)
    private String errorMessage; // 错误信息
    
    @Column(length = 64)
    private String hmacValue; // HMAC-SM3值，用于日志完整性校验
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 创建时间
}
