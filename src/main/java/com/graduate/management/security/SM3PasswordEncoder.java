package com.graduate.management.security;

import com.graduate.management.util.SM3Util;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * 基于SM3国密算法的密码编码器
 */
@Component
@Primary
@RequiredArgsConstructor
public class SM3PasswordEncoder implements PasswordEncoder {
    
    private final SM3Util sm3Util;
    private static final int SALT_LENGTH = 16; // 盐值长度
    
    /**
     * 使用SM3算法和随机盐值对密码进行编码
     *
     * @param rawPassword 原始密码
     * @return 编码后的密码（格式：盐值:哈希值）
     */
    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        // 生成随机盐值
        byte[] salt = generateSalt();
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        
        // 计算哈希值
        String hash = sm3Util.hash(saltBase64 + rawPassword.toString());
        
        // 拼接盐值和哈希值
        return saltBase64 + ":" + hash;
    }
    
    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 编码后的密码
     * @return 如果密码匹配则返回true
     */
    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        
        // 拆分盐值和哈希值
        String[] parts = encodedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        
        String salt = parts[0];
        String hash = parts[1];
        
        // 计算哈希值并比较
        String calculatedHash = sm3Util.hash(salt + rawPassword.toString());
        
        return hash.equals(calculatedHash);
    }
    
    /**
     * 生成随机盐值
     *
     * @return 随机盐值字节数组
     */
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
}
