package com.graduate.management.controller;

import com.graduate.management.dto.ApiResponse;
import com.graduate.management.dto.JwtResponse;
import com.graduate.management.dto.LoginRequest;
import com.graduate.management.dto.PasswordChangeRequest;
import com.graduate.management.entity.User;
import com.graduate.management.service.SystemLogService;
import com.graduate.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    private final SystemLogService systemLogService;
    
    @PostMapping("/login")
    public ApiResponse<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        try {
            JwtResponse response = userService.login(loginRequest);
            
            // 记录登录日志
            User user = userService.findByUsername(loginRequest.getUsername()).orElse(null);
            systemLogService.log("LOGIN", "USER", user != null ? user.getId() : null, user,
                    "用户登录", true, null, request);
            
            return ApiResponse.success("登录成功", response);
        } catch (Exception e) {
            // 记录登录失败日志
            systemLogService.log("LOGIN", "USER", null, null,
                    "登录失败: " + loginRequest.getUsername(), false, e.getMessage(), request);
            
            return ApiResponse.fail("登录失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/refresh")
    public ApiResponse<JwtResponse> refreshToken(@RequestParam String refreshToken) {
        try {
            JwtResponse response = userService.refreshToken(refreshToken);
            return ApiResponse.success("刷新令牌成功", response);
        } catch (Exception e) {
            return ApiResponse.fail("刷新令牌失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/change-password")
    public ApiResponse<?> changePassword(@Valid @RequestBody PasswordChangeRequest request, 
                                        HttpServletRequest servletRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            boolean result = userService.changePassword(username, request);
            
            // 记录修改密码日志
            User user = userService.findByUsername(username).orElse(null);
            systemLogService.log("CHANGE_PASSWORD", "USER", user != null ? user.getId() : null, user,
                    "修改密码", result, null, servletRequest);
            
            if (result) {
                return ApiResponse.success("密码修改成功");
            } else {
                return ApiResponse.fail("密码修改失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("密码修改失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            
            // 记录登出日志
            systemLogService.log("LOGOUT", "USER", user != null ? user.getId() : null, user,
                    "用户登出", true, null, request);
            
            return ApiResponse.success("登出成功");
        } catch (Exception e) {
            return ApiResponse.fail("登出失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/check-password-expiry")
    public ApiResponse<?> checkPasswordExpiry() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            boolean expired = userService.checkPasswordExpiry(username);
            
            if (expired) {
                return ApiResponse.success("密码已过期，请修改密码", true);
            } else {
                return ApiResponse.success("密码未过期", false);
            }
        } catch (Exception e) {
            return ApiResponse.fail("检查密码过期失败: " + e.getMessage());
        }
    }
}
