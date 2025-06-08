package com.graduate.management.service.impl;

import com.graduate.management.entity.College;
import com.graduate.management.entity.Major;
import com.graduate.management.repository.CollegeRepository;
import com.graduate.management.repository.MajorRepository;
import com.graduate.management.service.MajorService;
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
public class MajorServiceImpl implements MajorService {

    private final MajorRepository majorRepository;
    private final CollegeRepository collegeRepository;

    @Override
    public Major createMajor(Major major) {
        // 检查学院是否存在
        if (major.getCollege() == null || major.getCollege().getId() == null) {
            throw new RuntimeException("专业必须属于一个学院");
        }
        
        College college = collegeRepository.findById(major.getCollege().getId())
                .orElseThrow(() -> new EntityNotFoundException("学院不存在: " + major.getCollege().getId()));
        
        // 检查专业名称在同一学院内是否重复
        if (majorRepository.existsByNameAndCollege(major.getName(), college)) {
            throw new RuntimeException("该学院内专业名称已存在: " + major.getName());
        }
        
        // 检查专业编码是否重复
        if (major.getCode() != null && majorRepository.existsByCode(major.getCode())) {
            throw new RuntimeException("专业编码已存在: " + major.getCode());
        }
        
        major.setCollege(college);
        return majorRepository.save(major);
    }

    @Override
    public Major updateMajor(Long id, Major major) {
        Major existingMajor = majorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("专业不存在: " + id));

        // 如果更改了学院，需要验证新学院是否存在
        if (major.getCollege() != null && major.getCollege().getId() != null) {
            College college = collegeRepository.findById(major.getCollege().getId())
                    .orElseThrow(() -> new EntityNotFoundException("学院不存在: " + major.getCollege().getId()));
            
            // 检查专业名称在目标学院内是否重复
            if (!existingMajor.getName().equals(major.getName()) || 
                !existingMajor.getCollege().getId().equals(college.getId())) {
                if (majorRepository.existsByNameAndCollege(major.getName(), college)) {
                    throw new RuntimeException("该学院内专业名称已存在: " + major.getName());
                }
            }
            
            existingMajor.setCollege(college);
        }
        
        // 检查专业编码是否与其他专业重复
        if (major.getCode() != null && 
            !major.getCode().equals(existingMajor.getCode()) && 
            majorRepository.existsByCode(major.getCode())) {
            throw new RuntimeException("专业编码已存在: " + major.getCode());
        }

        existingMajor.setName(major.getName());
        existingMajor.setCode(major.getCode());
        existingMajor.setDescription(major.getDescription());

        return majorRepository.save(existingMajor);
    }

    @Override
    public void deleteMajor(Long id) {
        if (!majorRepository.existsById(id)) {
            throw new EntityNotFoundException("专业不存在: " + id);
        }
        majorRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Major> getMajorById(Long id) {
        return majorRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Major> getAllMajors() {
        return majorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Major> getAllMajors(Pageable pageable) {
        return majorRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Major> getMajorsByCollege(Long collegeId) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new EntityNotFoundException("学院不存在: " + collegeId));
        return majorRepository.findByCollege(college);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Major> getMajorsByCollege(Long collegeId, Pageable pageable) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new EntityNotFoundException("学院不存在: " + collegeId));
        // 注意：这里需要在Repository中添加分页查询方法
        return majorRepository.findAll(pageable); // 临时实现，需要优化
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Major> searchMajors(String keyword, Pageable pageable) {
        // 使用简单的搜索，可以根据需要扩展为更复杂的搜索
        return majorRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndCollege(String name, Long collegeId) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new EntityNotFoundException("学院不存在: " + collegeId));
        return majorRepository.existsByNameAndCollege(name, college);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return majorRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Major> findByNameAndCollege(String name, Long collegeId) {
        College college = collegeRepository.findById(collegeId)
                .orElseThrow(() -> new EntityNotFoundException("学院不存在: " + collegeId));
        return majorRepository.findByNameAndCollege(name, college);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Major> findByCode(String code) {
        return majorRepository.findByCode(code);
    }
}