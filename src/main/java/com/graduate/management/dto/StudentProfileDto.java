package com.graduate.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfileDto {
    
    private Long id;
    
    private String studentId;
    
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    @NotBlank(message = "性别不能为空")
    private String gender;
    
    @Size(min = 18, max = 18, message = "身份证号必须为18位")
    @NotBlank(message = "身份证号不能为空")
    private String idNumber;
    
    @NotNull(message = "学院不能为空")
    private Long collegeId;
    
    private String collegeName;
    
    @NotNull(message = "专业不能为空")
    private Long majorId;
    
    private String majorName;
    
    @NotBlank(message = "学位类型不能为空")
    private String degreeType;
    
    @NotNull(message = "导师不能为空")
    private Long supervisorId;
    
    private String supervisorName;
    
    @Past(message = "入学日期必须是过去的日期")
    private LocalDate enrollmentDate;
    
    private LocalDate expectedGraduationDate;
      private String currentAddress;
    
    private String permanentAddress;
    
    private String phone;
    
    private String email;
    
    private String address;
    
    private String emergencyContact;
    
    private String emergencyContactPhone;
    
    private String emergencyPhone;
    
    private String educationBackground;
    
    private String workExperience;
    
    private byte[] photo;
    
    private Boolean approved;
    
    private Long approverId;
    
    private String approverName;
}
