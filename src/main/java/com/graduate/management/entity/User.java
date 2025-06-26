package com.graduate.management.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username; // 用户名，学生的学号或其他用户的工号
    
    @Column(nullable = false)
    private String password; // 密码（加密后存储）
    
    @Column(nullable = false)
    private String name; // 用户姓名
    
    private String email; // 加密存储
    
    private String phone; // 加密存储
    
    private Boolean enabled = true; // 账号是否启用
    
    private Boolean accountNonLocked = true; // 账号是否锁定
    
    private Integer loginAttempts = 0; // 登录失败次数
    
    private LocalDateTime lockedTime; // 锁定时间
    
    private LocalDateTime lastPasswordChangeTime; // 最后修改密码时间
    
    private Boolean firstLogin = true; // 是否首次登录
    
    private LocalDateTime lastLoginTime; // 最后登录时间
    
    @CreationTimestamp
    private LocalDateTime createdAt; // 创建时间
    
    @UpdateTimestamp
    private LocalDateTime updatedAt; // 更新时间
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // 避免递归：不包含 roles 字段
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
