package com.graduate.management.util;

import com.graduate.management.dto.StudentProfileDto;
import com.graduate.management.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * DTO脱敏工具类
 * 用于在返回前端的DTO对象中对敏感信息进行脱敏处理
 */
@Component
@RequiredArgsConstructor
public class DtoMaskUtil {
    
    private final DataMaskUtil dataMaskUtil;
    
    /**
     * 对UserDto进行脱敏处理
     *
     * @param userDto 用户DTO
     * @return 脱敏后的用户DTO
     */
    public UserDto maskUserDto(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        
        // 脱敏电子邮件
        if (userDto.getEmail() != null) {
            userDto.setEmail(dataMaskUtil.maskEmail(userDto.getEmail()));
        }
        
        // 脱敏电话号码
        if (userDto.getPhone() != null) {
            userDto.setPhone(dataMaskUtil.maskPhoneNumber(userDto.getPhone()));
        }
        
        return userDto;
    }
    
    /**
     * 对StudentProfileDto进行脱敏处理
     *
     * @param studentProfileDto 学生档案DTO
     * @return 脱敏后的学生档案DTO
     */
    public StudentProfileDto maskStudentProfileDto(StudentProfileDto studentProfileDto) {
        if (studentProfileDto == null) {
            return null;
        }
        
        // 脱敏身份证号
        if (studentProfileDto.getIdNumber() != null) {
            studentProfileDto.setIdNumber(dataMaskUtil.maskIdNumber(studentProfileDto.getIdNumber()));
        }
        
        // 脱敏电话号码
        if (studentProfileDto.getPhone() != null) {
            studentProfileDto.setPhone(dataMaskUtil.maskPhoneNumber(studentProfileDto.getPhone()));
        }
        
        // 脱敏电子邮件
        if (studentProfileDto.getEmail() != null) {
            studentProfileDto.setEmail(dataMaskUtil.maskEmail(studentProfileDto.getEmail()));
        }
        
        // 脱敏家庭住址
        if (studentProfileDto.getAddress() != null) {
            studentProfileDto.setAddress(dataMaskUtil.maskAddress(studentProfileDto.getAddress()));
        }
        
        // 脱敏紧急联系人电话
        if (studentProfileDto.getEmergencyContact() != null && studentProfileDto.getEmergencyContactPhone() != null) {
            studentProfileDto.setEmergencyContactPhone(
                    dataMaskUtil.maskPhoneNumber(studentProfileDto.getEmergencyContactPhone()));
        }
        
        return studentProfileDto;
    }

    /**
     * 对StudentProfileDto进行脱敏处理
     *
     * @param dto 学生信息DTO
     * @param isSelfView 是否是学生本人查看
     * @return 脱敏后的学生信息DTO
     */
    public StudentProfileDto maskStudentProfile(StudentProfileDto dto, boolean isSelfView) {
        if (dto == null) {
            return null;
        }
        
        // 如果不是本人查看，则对敏感信息进行脱敏处理
        if (!isSelfView) {
            // 脱敏身份证号
            if (dto.getIdNumber() != null) {
                dto.setIdNumber(dataMaskUtil.maskIdNumber(dto.getIdNumber()));
            }
            
            // 脱敏地址信息
            if (dto.getCurrentAddress() != null) {
                dto.setCurrentAddress(dataMaskUtil.maskAddress(dto.getCurrentAddress()));
            }
            if (dto.getPermanentAddress() != null) {
                dto.setPermanentAddress(dataMaskUtil.maskAddress(dto.getPermanentAddress()));
            }
            
            // 脱敏联系电话
            if (dto.getEmergencyPhone() != null) {
                dto.setEmergencyPhone(dataMaskUtil.maskPhoneNumber(dto.getEmergencyPhone()));
            }
        }
        
        return dto;
    }
}