package com.graduate.management.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * 密码策略检查器
 * 实现密码复杂度要求：长度8位以上，包含数字、大小字母、特殊字符等混合组合
 */
@Component
public class PasswordPolicyChecker {
    
    @Value("${system.password.min-length:8}")
    private int minLength;
    
    /**
     * 检查密码是否符合密码策略
     *
     * @param password 密码
     * @return 是否符合密码策略
     */
    public boolean isValid(String password) {
        if (password == null || password.length() < minLength) {
            return false;
        }
        
        // 检查是否包含数字
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        
        // 检查是否包含小写字母
        boolean hasLower = Pattern.compile("[a-z]").matcher(password).find();
        
        // 检查是否包含大写字母
        boolean hasUpper = Pattern.compile("[A-Z]").matcher(password).find();
        
        // 检查是否包含特殊字符
        boolean hasSpecial = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]")
                .matcher(password).find();
        
        return hasDigit && hasLower && hasUpper && hasSpecial;
    }
    
    /**
     * 获取密码策略描述
     *
     * @return 密码策略描述
     */
    public String getPolicyDescription() {
        return "密码长度至少为" + minLength + "位，且必须包含数字、大写字母、小写字母和特殊字符。";
    }
}
