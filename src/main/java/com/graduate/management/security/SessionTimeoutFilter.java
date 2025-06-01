package com.graduate.management.security;

import com.graduate.management.entity.User;
import com.graduate.management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 会话超时过滤器
 * 检查用户最后活动时间，超过会话超时时间则自动退出
 */
@Component
@RequiredArgsConstructor
public class SessionTimeoutFilter extends OncePerRequestFilter {
    
    private final UserService userService;
    
    @Value("${system.password.session-timeout:30}")
    private int sessionTimeoutMinutes;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 获取当前认证用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            // 检查最后登录时间
            if (user.getLastLoginTime() != null) {
                LocalDateTime timeoutTime = user.getLastLoginTime().plusMinutes(sessionTimeoutMinutes);
                
                // 如果超过会话超时时间，清除认证
                if (LocalDateTime.now().isAfter(timeoutTime)) {
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"message\":\"会话已过期，请重新登录\"}");
                    response.setContentType("application/json");
                    return;
                }
            }
            
            // 更新最后登录时间
            try {
                user.setLastLoginTime(LocalDateTime.now());
                userService.updateUser(user);
            } catch (Exception e) {
                // 忽略更新错误，不影响主流程
                logger.warn("更新用户最后活动时间失败", e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
