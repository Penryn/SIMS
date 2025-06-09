package com.graduate.management.util;

import com.graduate.management.security.SM3PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 管理员用户创建工具类
 * 系统启动时，如果启用了此功能，则创建管理员账号
 */
@Component
@ConditionalOnProperty(name = "system.create-admin", havingValue = "true")
public class AdminAccountCreator implements CommandLineRunner {

    @Autowired
    private SM3PasswordEncoder sm3PasswordEncoder;
    
    @Override
    public void run(String... args) {
        // 创建管理员密码
        String adminPassword = "Admin@123456"; // 示例密码，请修改为强密码
        String encodedPassword = sm3PasswordEncoder.encode(adminPassword);
        
        System.out.println("\n\n=============================================");
        System.out.println("系统管理员账号创建工具");
        System.out.println("---------------------------------------------");
        System.out.println("用户名: admin");
        System.out.println("密码: " + adminPassword);
        System.out.println("加密后的密码: " + encodedPassword);
        System.out.println("请使用上面的加密密码替换SQL脚本中的密码占位符");
        System.out.println("=============================================\n\n");
        
        // 或者直接使用JdbcTemplate执行SQL语句创建管理员账号
        // jdbcTemplate.update("INSERT INTO users (username, password, ...) VALUES (?, ?, ...)", "admin", encodedPassword, ...);
    }
}
