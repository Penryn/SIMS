package com.graduate.management.service;

import com.graduate.management.dto.JwtResponse;
import com.graduate.management.dto.LoginRequest;
import com.graduate.management.dto.PasswordChangeRequest;
import com.graduate.management.dto.RegisterRequest;
import com.graduate.management.dto.UserDto;
import com.graduate.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {
    JwtResponse login(LoginRequest loginRequest);
    
    JwtResponse refreshToken(String refreshToken);
    
    JwtResponse register(RegisterRequest registerRequest);
    
    User createUser(User user);
    
    User updateUser(Long id, User user);
    
    void deleteUser(Long id);
    
    UserDto getUserById(Long id);
    
    UserDto getUserByUsername(String username);
    
    Page<UserDto> getAllUsers(Pageable pageable);
    
    List<UserDto> getUsersByRole(String role);
    
    boolean changePassword(String username, PasswordChangeRequest request);
    
    boolean resetPassword(Long userId);
    
    boolean lockUser(Long userId);
    
    boolean unlockUser(Long userId);
    
    boolean enableUser(Long userId);
    
    boolean disableUser(Long userId);
    
    Optional<User> findByUsername(String username);
    
    boolean checkPasswordExpiry(String username);
    
    void updateLoginAttempts(String username, int attempts);
    
    void updateAccountLockStatus(String username, boolean locked);
    
    void updateLastLoginTime(String username);
}
