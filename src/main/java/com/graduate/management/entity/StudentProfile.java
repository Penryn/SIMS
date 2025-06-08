package com.graduate.management.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "student_profiles")
public class StudentProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 关联的用户
    
    @Column(nullable = false, unique = true)
    private String studentId; // 学号
    
    @Column(nullable = false)
    private String name; // 姓名
    
    @Column(nullable = false)
    private String gender; // 性别
    
    @Column(nullable = false)
    private String idNumber; // 身份证号（加密存储）
    
    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college; // 所属学院
    
    @ManyToOne
    @JoinColumn(name = "major_id", nullable = false)
    private Major major; // 专业
    
    @Column(nullable = false)
    private String degreeType; // 学位类型（硕士/博士）
    
    @ManyToOne
    @JoinColumn(name = "supervisor_id", nullable = false)
    private User supervisor; // 导师
    
    private LocalDate enrollmentDate; // 入学日期
    
    private LocalDate expectedGraduationDate; // 预计毕业日期
    
    private String currentAddress; // 当前住址（加密存储）
    
    private String permanentAddress; // 永久住址（加密存储）
    
    private String emergencyContact; // 紧急联系人
    
    private String emergencyPhone; // 紧急联系电话（加密存储）
    
    @Column(length = 1000)
    private String educationBackground; // 教育背景
    
    @Column(length = 1000)
    private String workExperience; // 工作经历
    
    @Lob
    private byte[] photo; // 照片
    
    @Column(nullable = false)
    private Boolean approved = false; // 是否已通过审核
    
    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approver; // 审核人
    
    private LocalDateTime approveTime; // 审核时间
    
    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy; // 创建者
    
    @CreationTimestamp
    private LocalDateTime createdAt; // 创建时间
    
    @UpdateTimestamp
    private LocalDateTime updatedAt; // 更新时间
}
