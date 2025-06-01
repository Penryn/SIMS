package com.graduate.management.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "colleges")
public class College {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name; // 学院名称
    
    @Column(unique = true, length = 10)
    private String code; // 学院编码
    
    private String description; // 学院描述
    
    @OneToMany(mappedBy = "college")
    private Set<Major> majors = new HashSet<>();
    
    @CreationTimestamp
    private LocalDateTime createdAt; // 创建时间
    
    @UpdateTimestamp
    private LocalDateTime updatedAt; // 更新时间
}
