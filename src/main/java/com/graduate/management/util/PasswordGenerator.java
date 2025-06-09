package com.graduate.management.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于生成加密密码的临时工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordGenerator implements CommandLineRunner {
    
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        // 要加密的密码
        String password = "Admin@123456";
        
        // 使用PasswordEncoder加密密码
        String encodedPassword = passwordEncoder.encode(password);
        
        // 打印加密后的密码
        log.info("=========================================");
        log.info("原始密码: {}", password);
        log.info("SM3加密后的密码: {}", encodedPassword);
        log.info("=========================================");
    }
}
