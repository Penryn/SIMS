package com.graduate.management.controller;

import com.graduate.management.dto.ApiResponse;
import com.graduate.management.dto.UserDto;
import com.graduate.management.entity.User;
import com.graduate.management.service.SystemLogService;
import com.graduate.management.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final SystemLogService systemLogService;
    
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN') or hasRole('ROLE_GRADUATE_ADMIN')")
    public ApiResponse<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = "asc".equalsIgnoreCase(sortDir) 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDto> users = userService.getAllUsers(pageable);
        
        return ApiResponse.success("查询成功", users);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN') or hasRole('ROLE_GRADUATE_ADMIN') or #id == authentication.principal.id")
    public ApiResponse<UserDto> getUserById(@PathVariable Long id) {
        try {
            UserDto user = userService.getUserById(id);
            return ApiResponse.success("查询成功", user);
        } catch (Exception e) {
            return ApiResponse.fail("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/current")
    public ApiResponse<UserDto> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            UserDto user = userService.getUserByUsername(username);
            return ApiResponse.success("查询成功", user);
        } catch (Exception e) {
            return ApiResponse.fail("查询失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN') or hasRole('ROLE_GRADUATE_ADMIN')")
    public ApiResponse<List<UserDto>> getUsersByRole(@PathVariable String role) {
        try {
            List<UserDto> users = userService.getUsersByRole(role);
            return ApiResponse.success("查询成功", users);
        } catch (Exception e) {
            return ApiResponse.fail("查询失败: " + e.getMessage());
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    public ApiResponse<User> createUser(@Valid @RequestBody User user, HttpServletRequest request) {
        try {
            User createdUser = userService.createUser(user);
            
            // 记录创建用户日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username).orElse(null);
            
            systemLogService.log("CREATE", "USER", createdUser.getId(), currentUser,
                    "创建用户: " + createdUser.getUsername(), true, null, request);
            
            return ApiResponse.success("创建成功", createdUser);
        } catch (Exception e) {
            return ApiResponse.fail("创建失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN') or #id == authentication.principal.id")
    public ApiResponse<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user, 
                                       HttpServletRequest request) {
        try {
            User updatedUser = userService.updateUser(id, user);
            
            // 记录更新用户日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username).orElse(null);
            
            systemLogService.log("UPDATE", "USER", updatedUser.getId(), currentUser,
                    "更新用户: " + updatedUser.getUsername(), true, null, request);
            
            return ApiResponse.success("更新成功", updatedUser);
        } catch (Exception e) {
            return ApiResponse.fail("更新失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    public ApiResponse<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserDto user = userService.getUserById(id);
            userService.deleteUser(id);
            
            // 记录删除用户日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username).orElse(null);
            
            systemLogService.log("DELETE", "USER", id, currentUser,
                    "删除用户: " + user.getUsername(), true, null, request);
            
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.fail("删除失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    public ApiResponse<?> resetPassword(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserDto user = userService.getUserById(id);
            boolean result = userService.resetPassword(id);
            
            // 记录重置密码日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username).orElse(null);
            
            systemLogService.log("RESET_PASSWORD", "USER", id, currentUser,
                    "重置用户密码: " + user.getUsername(), result, null, request);
            
            if (result) {
                return ApiResponse.success("密码重置成功");
            } else {
                return ApiResponse.fail("密码重置失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("密码重置失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/lock")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    public ApiResponse<?> lockUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserDto user = userService.getUserById(id);
            boolean result = userService.lockUser(id);
            
            // 记录锁定用户日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username).orElse(null);
            
            systemLogService.log("LOCK", "USER", id, currentUser,
                    "锁定用户: " + user.getUsername(), result, null, request);
            
            if (result) {
                return ApiResponse.success("用户锁定成功");
            } else {
                return ApiResponse.fail("用户锁定失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("用户锁定失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    public ApiResponse<?> unlockUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserDto user = userService.getUserById(id);
            boolean result = userService.unlockUser(id);
            
            // 记录解锁用户日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username).orElse(null);
            
            systemLogService.log("UNLOCK", "USER", id, currentUser,
                    "解锁用户: " + user.getUsername(), result, null, request);
            
            if (result) {
                return ApiResponse.success("用户解锁成功");
            } else {
                return ApiResponse.fail("用户解锁失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("用户解锁失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/enable")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    public ApiResponse<?> enableUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserDto user = userService.getUserById(id);
            boolean result = userService.enableUser(id);
            
            // 记录启用用户日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username).orElse(null);
            
            systemLogService.log("ENABLE", "USER", id, currentUser,
                    "启用用户: " + user.getUsername(), result, null, request);
            
            if (result) {
                return ApiResponse.success("用户启用成功");
            } else {
                return ApiResponse.fail("用户启用失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("用户启用失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/disable")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SYSTEM_ADMIN')")
    public ApiResponse<?> disableUser(@PathVariable Long id, HttpServletRequest request) {
        try {
            UserDto user = userService.getUserById(id);
            boolean result = userService.disableUser(id);
            
            // 记录禁用用户日志
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userService.findByUsername(username).orElse(null);
            
            systemLogService.log("DISABLE", "USER", id, currentUser,
                    "禁用用户: " + user.getUsername(), result, null, request);
            
            if (result) {
                return ApiResponse.success("用户禁用成功");
            } else {
                return ApiResponse.fail("用户禁用失败");
            }
        } catch (Exception e) {
            return ApiResponse.fail("用户禁用失败: " + e.getMessage());
        }
    }
}
