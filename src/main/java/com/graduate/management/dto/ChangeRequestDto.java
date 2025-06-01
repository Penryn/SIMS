package com.graduate.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestDto {
    
    private Long id;
    
    @NotNull(message = "学生ID不能为空")
    private Long studentProfileId;
    
    private String studentName;
    
    private String studentId;
    
    private Long requesterId;
    
    private String requesterName;
    
    @NotBlank(message = "字段名不能为空")
    private String fieldName;
    
    @NotEmpty(message = "修改前的值不能为空")
    private String oldValue;
    
    @NotEmpty(message = "修改后的值不能为空")
    private String newValue;
    
    private String status;
    
    private Long reviewerId;
    
    private String reviewerName;
    
    private String comment;
    
    private LocalDateTime reviewTime;
    
    private LocalDateTime createdAt;
}
