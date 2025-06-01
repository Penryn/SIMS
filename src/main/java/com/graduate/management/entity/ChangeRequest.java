package com.graduate.management.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "change_requests")
public class ChangeRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_profile_id", nullable = false)
    private StudentProfile studentProfile; // 关联的学生学籍信息
    
    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // 发起请求的用户
    
    @Column(nullable = false)
    private String fieldName; // 请求修改的字段名称
    
    @Column(nullable = false, length = 1000)
    private String oldValue; // 修改前的值
    
    @Column(nullable = false, length = 1000)
    private String newValue; // 修改后的值
    
    @Column(nullable = false)
    private String status; // 请求状态：PENDING, APPROVED, REJECTED
    
    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer; // 审核者
    
    private String comment; // 审核意见
    
    private LocalDateTime reviewTime; // 审核时间
    
    @CreationTimestamp
    private LocalDateTime createdAt; // 创建时间
    
    @UpdateTimestamp
    private LocalDateTime updatedAt; // 更新时间
}
