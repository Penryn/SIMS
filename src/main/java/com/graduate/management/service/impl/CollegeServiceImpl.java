package com.graduate.management.service.impl;

import com.graduate.management.entity.College;
import com.graduate.management.repository.CollegeRepository;
import com.graduate.management.service.CollegeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CollegeServiceImpl implements CollegeService {

    private final CollegeRepository collegeRepository;

    @Override
    public College createCollege(College college) {
        // 检查学院名称是否重复
        if (collegeRepository.existsByName(college.getName())) {
            throw new RuntimeException("学院名称已存在: " + college.getName());
        }
        
        // 检查学院编码是否重复
        if (college.getCode() != null && collegeRepository.existsByCode(college.getCode())) {
            throw new RuntimeException("学院编码已存在: " + college.getCode());
        }
        
        return collegeRepository.save(college);
    }

    @Override
    public College updateCollege(Long id, College college) {
        College existingCollege = collegeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("学院不存在: " + id));

        // 检查学院名称是否与其他学院重复
        if (!existingCollege.getName().equals(college.getName()) && 
            collegeRepository.existsByName(college.getName())) {
            throw new RuntimeException("学院名称已存在: " + college.getName());
        }
        
        // 检查学院编码是否与其他学院重复
        if (college.getCode() != null && 
            !college.getCode().equals(existingCollege.getCode()) && 
            collegeRepository.existsByCode(college.getCode())) {
            throw new RuntimeException("学院编码已存在: " + college.getCode());
        }

        existingCollege.setName(college.getName());
        existingCollege.setCode(college.getCode());
        existingCollege.setDescription(college.getDescription());

        return collegeRepository.save(existingCollege);
    }

    @Override
    public void deleteCollege(Long id) {
        if (!collegeRepository.existsById(id)) {
            throw new EntityNotFoundException("学院不存在: " + id);
        }
        collegeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<College> getCollegeById(Long id) {
        return collegeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<College> getAllColleges() {
        return collegeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<College> getAllColleges(Pageable pageable) {
        return collegeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<College> searchColleges(String keyword, Pageable pageable) {
        // 使用简单的名称搜索，可以根据需要扩展为更复杂的搜索
        return collegeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return collegeRepository.existsByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return collegeRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<College> findByName(String name) {
        return collegeRepository.findByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<College> findByCode(String code) {
        return collegeRepository.findByCode(code);
    }
}