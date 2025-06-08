package com.graduate.management.service.impl;

import com.graduate.management.dto.ChangeRequestDto;
import com.graduate.management.entity.ChangeRequest;
import com.graduate.management.entity.StudentProfile;
import com.graduate.management.entity.User;
import com.graduate.management.repository.ChangeRequestRepository;
import com.graduate.management.repository.StudentProfileRepository;
import com.graduate.management.repository.UserRepository;
import com.graduate.management.service.ChangeRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChangeRequestServiceImpl implements ChangeRequestService {

    private final ChangeRequestRepository changeRequestRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChangeRequestDto createChangeRequest(ChangeRequestDto dto) {
        StudentProfile studentProfile = studentProfileRepository.findById(dto.getStudentProfileId())
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        
        User requester = userRepository.findById(dto.getRequesterId())
                .orElseThrow(() -> new RuntimeException("请求者不存在"));
        
        ChangeRequest changeRequest = new ChangeRequest();
        changeRequest.setStudentProfile(studentProfile);
        changeRequest.setRequester(requester);
        changeRequest.setFieldName(dto.getFieldName());
        changeRequest.setOldValue(dto.getOldValue());
        changeRequest.setNewValue(dto.getNewValue());
        changeRequest.setStatus("PENDING"); // 设置初始状态为待审核
        
        ChangeRequest saved = changeRequestRepository.save(changeRequest);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public ChangeRequestDto updateChangeRequest(Long id, ChangeRequestDto dto) {
        ChangeRequest changeRequest = changeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("变更请求不存在"));
        
        // 只能更新状态为PENDING的请求
        if (!"PENDING".equals(changeRequest.getStatus())) {
            throw new RuntimeException("只能更新待审核状态的变更请求");
        }
        
        if (dto.getFieldName() != null) {
            changeRequest.setFieldName(dto.getFieldName());
        }
        if (dto.getOldValue() != null) {
            changeRequest.setOldValue(dto.getOldValue());
        }
        if (dto.getNewValue() != null) {
            changeRequest.setNewValue(dto.getNewValue());
        }
        
        ChangeRequest updated = changeRequestRepository.save(changeRequest);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void deleteChangeRequest(Long id) {
        ChangeRequest changeRequest = changeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("变更请求不存在"));
        
        // 只能删除状态为PENDING的请求
        if (!"PENDING".equals(changeRequest.getStatus())) {
            throw new RuntimeException("只能删除待审核状态的变更请求");
        }
        
        changeRequestRepository.deleteById(id);
    }    @Override
    public ChangeRequestDto getChangeRequestById(Long id, User requester) {
        ChangeRequest changeRequest = changeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("变更请求不存在"));
        
        // 检查权限：只有申请人、审核人或管理员可以查看
        // 这里可以根据业务需求进行权限检查
        
        return convertToDto(changeRequest);
    }

    @Override
    public List<ChangeRequestDto> getChangeRequestsByStudentProfileId(Long studentProfileId) {
        StudentProfile studentProfile = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new RuntimeException("学生学籍信息不存在"));
        
        return changeRequestRepository.findByStudentProfile(studentProfile)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ChangeRequestDto> getPendingChangeRequests(Pageable pageable) {
        return changeRequestRepository.findByStatus("PENDING", pageable)
                .map(this::convertToDto);
    }    @Override
    @Transactional
    public ChangeRequest approveChangeRequest(Long id, User reviewer, String comment) {
        ChangeRequest changeRequest = changeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("变更请求不存在"));
        
        // 只能审核状态为PENDING的请求
        if (!"PENDING".equals(changeRequest.getStatus())) {
            throw new RuntimeException("只能审核待审核状态的变更请求");
        }
        
        // 更新审核信息
        changeRequest.setStatus("APPROVED");
        changeRequest.setReviewer(reviewer);
        changeRequest.setComment(comment);
        changeRequest.setReviewTime(LocalDateTime.now());
        
        changeRequestRepository.save(changeRequest);
        
        // 应用变更到学生学籍信息
        StudentProfile studentProfile = changeRequest.getStudentProfile();
        applyChange(studentProfile, changeRequest.getFieldName(), changeRequest.getNewValue());
        studentProfileRepository.save(studentProfile);
        
        return changeRequest;
    }

    @Override
    @Transactional
    public ChangeRequest rejectChangeRequest(Long id, User reviewer, String comment) {
        ChangeRequest changeRequest = changeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("变更请求不存在"));
        
        // 只能审核状态为PENDING的请求
        if (!"PENDING".equals(changeRequest.getStatus())) {
            throw new RuntimeException("只能审核待审核状态的变更请求");
        }
        
        // 更新审核信息
        changeRequest.setStatus("REJECTED");
        changeRequest.setReviewer(reviewer);
        changeRequest.setComment(comment);
        changeRequest.setReviewTime(LocalDateTime.now());
        
        changeRequest = changeRequestRepository.save(changeRequest);
        return changeRequest;
    }
      // 将实体转换为DTO
    @Override
    public ChangeRequestDto convertToDto(ChangeRequest changeRequest) {
        ChangeRequestDto dto = new ChangeRequestDto();
        dto.setId(changeRequest.getId());
        dto.setStudentProfileId(changeRequest.getStudentProfile().getId());
        dto.setStudentName(changeRequest.getStudentProfile().getName());
        dto.setStudentId(changeRequest.getStudentProfile().getStudentId());
        dto.setRequesterId(changeRequest.getRequester().getId());
        dto.setRequesterName(changeRequest.getRequester().getName());
        dto.setFieldName(changeRequest.getFieldName());
        dto.setOldValue(changeRequest.getOldValue());
        dto.setNewValue(changeRequest.getNewValue());
        dto.setStatus(changeRequest.getStatus());
        
        if (changeRequest.getReviewer() != null) {
            dto.setReviewerId(changeRequest.getReviewer().getId());
            dto.setReviewerName(changeRequest.getReviewer().getName());
        }
        
        dto.setComment(changeRequest.getComment());
        dto.setReviewTime(changeRequest.getReviewTime());
        dto.setCreatedAt(changeRequest.getCreatedAt());
        
        return dto;
    }
    
    // 应用变更到学生学籍信息
    private void applyChange(StudentProfile studentProfile, String fieldName, String newValue) {
        // 根据字段名应用变更
        switch (fieldName) {
            case "name":
                studentProfile.setName(newValue);
                break;
            case "gender":
                studentProfile.setGender(newValue);
                break;
            case "idNumber":
                // 注意：身份证号等敏感信息需要加密存储
                // 在实际应用中，这里应该先加密再存储
                // studentProfile.setIdNumber(sm4Util.encrypt(newValue));
                break;
            case "degreeType":
                studentProfile.setDegreeType(newValue);
                break;            // 可以添加更多字段的处理
            default:
                throw new RuntimeException("不支持的字段变更: " + fieldName);
        }
    }
    
    // 新增方法实现
    @Override
    public Page<ChangeRequestDto> getAllChangeRequests(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return changeRequestRepository.findByStatus(status, pageable)
                    .map(this::convertToDto);
        } else {
            return changeRequestRepository.findAll(pageable)
                    .map(this::convertToDto);
        }
    }
    
    @Override
    public Page<ChangeRequestDto> getCollegeChangeRequests(User user, String status, Pageable pageable) {
        // 根据用户的学院权限过滤变更申请
        // 这里需要根据实际业务逻辑实现，比如通过用户的学院ID来过滤
        if (status != null && !status.isEmpty()) {
            return changeRequestRepository.findByStatus(status, pageable)
                    .map(this::convertToDto);
        } else {
            return changeRequestRepository.findAll(pageable)
                    .map(this::convertToDto);
        }
    }
    
    @Override
    @Transactional
    public ChangeRequest cancelChangeRequest(Long id, User user) {
        ChangeRequest changeRequest = changeRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("变更请求不存在"));
        
        // 只能取消状态为PENDING的请求
        if (!"PENDING".equals(changeRequest.getStatus())) {
            throw new RuntimeException("只能取消待审核状态的变更请求");
        }
        
        // 检查权限：只有申请人可以取消
        if (!changeRequest.getRequester().getId().equals(user.getId())) {
            throw new RuntimeException("只有申请人可以取消变更请求");
        }
        
        // 更新状态为已取消
        changeRequest.setStatus("CANCELLED");
        changeRequest.setReviewTime(LocalDateTime.now());
        
        return changeRequestRepository.save(changeRequest);
    }
}
