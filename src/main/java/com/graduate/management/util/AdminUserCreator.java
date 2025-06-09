package com.graduate.management.util;

import com.graduate.management.entity.Role;
import com.graduate.management.entity.User;
import com.graduate.management.repository.RoleRepository;
import com.graduate.management.repository.UserRepository;
import com.graduate.management.security.SM3PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 管理员用户自动创建工具
 * 系统启动时，如果启用了此功能，则直接创建管理员账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "system.create-admin", havingValue = "true")
public class AdminUserCreator implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SM3PasswordEncoder sm3PasswordEncoder;
    
    @Override
    @Transactional
    public void run(String... args) {
        // 检查是否已存在admin用户
        if (userRepository.existsByUsername("admin")) {
            log.info("系统管理员账号已存在，跳过创建");
            return;
        }

        log.info("开始创建系统管理员账号...");
        
        // 创建角色（如果不存在）
        createRoleIfNotExists("ROLE_SYSTEM_ADMIN", "系统管理员");
        createRoleIfNotExists("ROLE_AUDIT_ADMIN", "审计管理员");
        
        // 创建管理员用户
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setName("系统管理员");
        adminUser.setEmail("admin@example.com");
        adminUser.setPhone("13800000000");
        
        // 设置密码 Admin@123456
        String adminPassword = "Admin@123456";
        adminUser.setPassword(sm3PasswordEncoder.encode(adminPassword));
        
        // 设置其他属性
        adminUser.setEnabled(true);
        adminUser.setAccountNonLocked(true);
        adminUser.setFirstLogin(false);
        adminUser.setLoginAttempts(0);
        adminUser.setLastPasswordChangeTime(LocalDateTime.now());
        
        // 分配角色
        Set<Role> roles = new HashSet<>();
        Optional<Role> systemAdminRole = roleRepository.findByName("ROLE_SYSTEM_ADMIN");
        systemAdminRole.ifPresent(roles::add);
        
        adminUser.setRoles(roles);
        
        // 保存用户
        userRepository.save(adminUser);
        
        log.info("系统管理员账号创建成功");
        log.info("------------------------------------------------");
        log.info("用户名: admin");
        log.info("密码: {}", adminPassword);
        log.info("------------------------------------------------");
        log.info("请记住密码并在登录后立即修改!");
    }
    
    /**
     * 如果角色不存在，则创建角色
     */
    private void createRoleIfNotExists(String roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription(description);
            roleRepository.save(role);
            log.info("创建角色: {}", roleName);
        }
    }
}
