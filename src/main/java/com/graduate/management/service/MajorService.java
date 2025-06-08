package com.graduate.management.service;

import com.graduate.management.entity.Major;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MajorService {
    
    Major createMajor(Major major);
    
    Major updateMajor(Long id, Major major);
    
    void deleteMajor(Long id);
    
    Optional<Major> getMajorById(Long id);
    
    List<Major> getAllMajors();
    
    Page<Major> getAllMajors(Pageable pageable);
    
    List<Major> getMajorsByCollege(Long collegeId);
    
    Page<Major> getMajorsByCollege(Long collegeId, Pageable pageable);
    
    Page<Major> searchMajors(String keyword, Pageable pageable);
    
    boolean existsByNameAndCollege(String name, Long collegeId);
    
    boolean existsByCode(String code);
    
    Optional<Major> findByNameAndCollege(String name, Long collegeId);
    
    Optional<Major> findByCode(String code);
}