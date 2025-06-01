package com.graduate.management.util;

import org.springframework.stereotype.Component;

/**
 * 敏感数据脱敏工具类
 * 用于在页面上对关键信息进行脱敏显示
 */
@Component
public class DataMaskUtil {
    
    /**
     * 对身份证号进行脱敏
     * 规则：保留前6位和后4位，中间用*替代
     * 
     * 例如：110101199001011234 -> 110101********1234
     *
     * @param idNumber 身份证号
     * @return 脱敏后的身份证号
     */
    public String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.length() < 11) {
            return idNumber;
        }
        
        int length = idNumber.length();
        return idNumber.substring(0, 6) + "*".repeat(length - 10) + idNumber.substring(length - 4);
    }
    
    /**
     * 对电话号码进行脱敏
     * 规则：保留前3位和后4位，中间用*替代
     * 
     * 例如：13812345678 -> 138****5678
     *
     * @param phoneNumber 电话号码
     * @return 脱敏后的电话号码
     */
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return phoneNumber;
        }
        
        int length = phoneNumber.length();
        return phoneNumber.substring(0, 3) + "*".repeat(length - 7) + phoneNumber.substring(length - 4);
    }
    
    /**
     * 对邮箱进行脱敏
     * 规则：邮箱账号部分仅显示前3位，其余用*代替
     * 
     * 例如：example@domain.com -> exa****@domain.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email;
        }
        
        int atIndex = email.indexOf('@');
        if (atIndex <= 3) {
            return email.substring(0, 1) + "*".repeat(atIndex - 1) + email.substring(atIndex);
        } else {
            return email.substring(0, 3) + "*".repeat(atIndex - 3) + email.substring(atIndex);
        }
    }
    
    /**
     * 对地址进行脱敏
     * 规则：保留前6个字符，其余用*替换
     *
     * @param address 地址
     * @return 脱敏后的地址
     */
    public String maskAddress(String address) {
        if (address == null || address.length() <= 6) {
            return address;
        }
        
        return address.substring(0, 6) + "*".repeat(Math.min(address.length() - 6, 10)) + "...";
    }
    
    /**
     * 对姓名进行脱敏
     * 规则：仅显示姓，名字用*替代
     * 
     * 例如：张三 -> 张*
     *
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        if (name.length() == 1) {
            return name;
        } else if (name.length() == 2) {
            return name.substring(0, 1) + "*";
        } else {
            return name.substring(0, 1) + "*".repeat(name.length() - 1);
        }
    }
}
