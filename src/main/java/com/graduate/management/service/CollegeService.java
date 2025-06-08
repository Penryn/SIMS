package com.graduate.management.service;

import com.graduate.management.entity.College;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CollegeService {
    
    College createCollege(College college);
    
    College updateCollege(Long id, College college);
    
    void deleteCollege(Long id);
    
    Optional<College> getCollegeById(Long id);
    
    List<College> getAllColleges();
    
    Page<College> getAllColleges(Pageable pageable);
    
    Page<College> searchColleges(String keyword, Pageable pageable);
    
    boolean existsByName(String name);
    
    boolean existsByCode(String code);
    
    Optional<College> findByName(String name);
    
    Optional<College> findByCode(String code);
}