package com.graduate.management.security;

import com.graduate.management.entity.User;
import com.graduate.management.service.SystemLogService;
import com.graduate.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 登录失败处理器
 * 实现登录失败限制次数和账号锁定功能
 */
@Component
@RequiredArgsConstructor
public class LoginAttemptService {
    
    private final UserService userService;
    private final SystemLogService systemLogService;
    
    @Value("${system.password.login-retry-limit:5}")
    private int maxAttempts;
    
    @Value("${system.password.lock-duration:30}")
    private int lockDuration;
    
    /**
     * 处理登录失败
     *
     * @param username 用户名
     * @param exception 认证异常
     * @param request HTTP请求
     */
    public void loginFailed(String username, AuthenticationException exception, HttpServletRequest request) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        try {
            User user = userService.findByUsername(username);
            
            // 更新登录失败次数
            int failures = user.getLoginAttempts() + 1;
            user.setLoginAttempts(failures);
            
            // 如果失败次数达到上限，锁定账号
            if (failures >= maxAttempts) {
                user.setAccountNonLocked(false);
                user.setLockedTime(LocalDateTime.now());
                
                // 记录账号锁定日志
                systemLogService.log(
                        "ACCOUNT_LOCKED",
                        "USER",
                        user.getId(),
                        null,
                        String.format("账号被锁定，失败次数: %d", failures),
                        true,
                        null,
                        request
                );
            }
            
            userService.updateUser(user);
            
            // 记录登录失败日志
            systemLogService.log(
                    "LOGIN_FAILED",
                    "USER",
                    user.getId(),
                    null,
                    String.format("登录失败，原因: %s", exception.getMessage()),
                    false,
                    exception.getMessage(),
                    request
            );
        } catch (Exception e) {
            // 用户不存在或其他错误，只记录日志
            systemLogService.log(
                    "LOGIN_FAILED",
                    "USER",
                    null,
                    null,
                    String.format("用户 %s 登录失败", username),
                    false,
                    e.getMessage(),
                    request
            );
        }
    }
    
    /**
     * 处理登录成功
     *
     * @param username 用户名
     * @param request HTTP请求
     */
    public void loginSucceeded(String username, HttpServletRequest request) {
        try {
            User user = userService.findByUsername(username);
            
            // 重置登录失败次数
            user.setLoginAttempts(0);
            
            // 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            
            userService.updateUser(user);
            
            // 记录登录成功日志
            systemLogService.log(
                    "LOGIN_SUCCESS", 
                    "USER", 
                    user.getId(),
                    user,
                    "用户登录成功", 
                    true, 
                    null, 
                    request
            );
        } catch (Exception ignored) {
            // 忽略错误
        }
    }
    
    /**
     * 检查账号是否锁定
     *
     * @param username 用户名
     * @return 如果账号锁定返回true
     */
    public boolean isAccountLocked(String username) {
        try {
            User user = userService.findByUsername(username);
            
            if (!user.getAccountNonLocked() && user.getLockedTime() != null) {
                // 检查锁定时间是否已过期
                LocalDateTime unlockTime = user.getLockedTime().plusMinutes(lockDuration);
                
                // 如果已过锁定时间，则解锁账号
                if (LocalDateTime.now().isAfter(unlockTime)) {
                    user.setAccountNonLocked(true);
                    user.setLoginAttempts(0);
                    user.setLockedTime(null);
                    userService.updateUser(user);
                    return false;
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取账号解锁时间
     *
     * @param username 用户名
     * @return 解锁时间或null
     */
    public LocalDateTime getUnlockTime(String username) {
        try {
            User user = userService.findByUsername(username);
            
            if (!user.getAccountNonLocked() && user.getLockedTime() != null) {
                return user.getLockedTime().plusMinutes(lockDuration);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
