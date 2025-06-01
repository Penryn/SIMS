package com.graduate.management.repository;

import com.graduate.management.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CollegeRepository extends JpaRepository<College, Long> {
    
    Optional<College> findByName(String name);
    
    Optional<College> findByCode(String code);
    
    boolean existsByName(String name);
    
    boolean existsByCode(String code);
}
