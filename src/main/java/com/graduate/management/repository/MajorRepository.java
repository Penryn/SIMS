package com.graduate.management.repository;

import com.graduate.management.entity.College;
import com.graduate.management.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major, Long> {
    
    Optional<Major> findByName(String name);
    
    Optional<Major> findByCode(String code);
    
    List<Major> findByCollege(College college);
    
    boolean existsByNameAndCollege(String name, College college);
    
    boolean existsByCode(String code);
}
