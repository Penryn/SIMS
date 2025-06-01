package com.graduate.management.util;

import com.graduate.management.entity.Major;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 学号生成器
 * 基于专业编码、学位类型自动生成学号
 */
@Component
public class StudentIdGenerator {

    /**
     * 生成学号
     * 规则: 入学年份(4位) + 专业代码(4位) + 学位类型代码(1位) + 序号(3位)
     * 学位类型: 1-硕士, 2-博士
     *
     * @param major            专业
     * @param degreeType       学位类型
     * @param enrollmentYear   入学年份
     * @param sequenceNumber   序号
     * @return 生成的学号
     */
    public String generateStudentId(Major major, String degreeType, int enrollmentYear, int sequenceNumber) {
        // 获取专业代码
        String majorCode = major.getCode();
        
        // 获取学位类型代码
        String degreeTypeCode = "1"; // 默认硕士
        if ("博士".equals(degreeType)) {
            degreeTypeCode = "2";
        }
        
        // 格式化序号为3位数字, 不足补0
        String sequence = String.format("%03d", sequenceNumber);
        
        // 拼接学号
        return String.valueOf(enrollmentYear) + majorCode + degreeTypeCode + sequence;
    }
    
    /**
     * 根据当前日期和专业代码生成学号
     *
     * @param major          专业
     * @param degreeType     学位类型
     * @param sequenceNumber 序号
     * @return 生成的学号
     */
    public String generateStudentId(Major major, String degreeType, int sequenceNumber) {
        int year = LocalDate.now().getYear();
        return generateStudentId(major, degreeType, year, sequenceNumber);
    }
}
