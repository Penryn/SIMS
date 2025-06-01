package com.graduate.management.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "roles")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name; // 角色名称
    
    @Column(length = 500)
    private String description; // 角色描述
    
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();
}
