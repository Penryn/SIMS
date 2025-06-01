package com.graduate.management.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "majors")
public class Major {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name; // 专业名称
    
    @Column(unique = true, length = 10)
    private String code; // 专业编码
    
    private String description; // 专业描述
    
    @ManyToOne
    @JoinColumn(name = "college_id", nullable = false)
    private College college; // 所属学院
    
    @CreationTimestamp
    private LocalDateTime createdAt; // 创建时间
    
    @UpdateTimestamp
    private LocalDateTime updatedAt; // 更新时间
}
