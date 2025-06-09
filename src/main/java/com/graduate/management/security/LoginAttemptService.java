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
import java.util.Optional;

/**
 * 登录处理器
 * 负责登录事件的日志记录
 */
@Component
@RequiredArgsConstructor
public class LoginAttemptService {
    
    private final UserService userService;
    private final SystemLogService systemLogService;
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
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
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
            }
        } catch (Exception e) {
            // 记录系统异常日志
            systemLogService.log(
                    "LOGIN_FAILED",
                    "USER",
                    null,
                    null,
                    String.format("登录失败处理异常，用户名: %s", username),
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
            Optional<User> userOptional = userService.findByUsername(username);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                
                // 更新最后登录时间
                user.setLastLoginTime(LocalDateTime.now());
                
                userService.updateUser(user.getId(), user);
                
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
            }
        } catch (Exception e) {
            // 记录系统异常日志
            systemLogService.log(
                    "LOGIN_SUCCESS",
                    "USER",
                    null,
                    null,
                    String.format("登录成功处理异常，用户名: %s", username),
                    false,
                    e.getMessage(),
                    request
            );
        }
    }
}
